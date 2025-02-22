package com.standalone.todos;

import android.content.ContentValues;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.standalone.core.App;
import com.standalone.core.ext.ApiService;
import com.standalone.core.utils.Json;
import com.standalone.todos.local.todos.Todo;
import com.standalone.todos.local.todos.TodoDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import okhttp3.Response;

public class SyncManager {
    public static final long SYNCED_LATENCY = Long.parseLong(App.loadEnv().getProperty("SYNCED_LATENCY"));
    private final TodoDao dao;
    private final ApiService<Todo> apiService;
    private final List<CompletableFuture<Response>> futures;

    public SyncManager() {
        this.dao = new TodoDao();
        this.apiService = new ApiService<>();
        this.futures = new ArrayList<>();
    }

    public void startSync() throws IOException, ExecutionException, InterruptedException {
        // syncing remote data to local
        futures.clear();
        for (Todo t : fetchDataFromServer()) {
            insertOrUpdate(t);
        }

        if (futures.size() == 0) {
            syncToRemote();
            return;
        }

        ApiService.join(futures).thenAccept(__ -> {
            // sync data to remote
            syncToRemote();
        });
    }

    public void insertOrUpdate(Todo remoteData) {
        Todo localData = dao.getByServerId(remoteData.getServerId());
        long id;
        if (localData == null) {
            id = dao.create(remoteData);
        } else {
            id = localData.getId();
            if (localData.isDeleted) {
                dao.delete(localData.getId());
                CompletableFuture<Response> f = apiService.delete(localData.getServerId());
                futures.add(f);
            } else {
                // Conflict resolution
                resolveConflict(localData, remoteData);
            }
        }
        dao.notifySynced(id);
    }

    private void syncToRemote() {
        for (Todo todo : dao.getAllUnsynced()) {
            System.out.println(todo.string());
            if (todo.isDeleted || todo.getServerId() > 0) {
                dao.delete(todo.getId());
            } else {
                apiService.insert(todo).thenAccept(response -> {
                    try {
                        if (!response.isSuccessful() || response.body() == null) return;
                        String s = response.body().string();
                        Todo remoteData = Json.parse(s, Todo.class);
                        ContentValues cv = new ContentValues();
                        cv.put("server_id", remoteData.getServerId());
                        dao.update(todo.getId(), cv);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).join();
            }

            dao.notifySynced(todo.getId());
        }
    }

    private void resolveConflict(Todo localData, Todo remoteData) {
        // Last updated wins
        if (remoteData.updatedAt().after(localData.updatedAt())) {
            dao.update(localData.getId(), Todo.asContentValues(remoteData));
        } else if (remoteData.updatedAt().before(localData.updatedAt())) {
            try {
                System.out.println(Json.stringify(localData));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            CompletableFuture<Response> f = apiService.update(remoteData.getServerId(), localData);
            futures.add(f);
        }
    }

    private List<Todo> fetchDataFromServer() throws ExecutionException, InterruptedException, IOException {
        Response response = apiService.fetchAll().get();
        if (response.body() == null) throw new IOException();

        return Json.parseList(response.body().string(), Todo.class);
    }
}
