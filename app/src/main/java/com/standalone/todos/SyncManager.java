package com.standalone.todos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.standalone.core.ext.ApiService;
import com.standalone.core.utils.Json;
import com.standalone.todos.local.todos.Todo;
import com.standalone.todos.local.todos.TodoDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import okhttp3.Callback;
import okhttp3.Response;

public class SyncManager {
    private final TodoDao dao;
    private final ApiService<Todo> apiService;
    private final List<CompletableFuture<Response>> futureList;

    public SyncManager() {
        this.dao = new TodoDao();
        this.apiService = new ApiService<>();
        this.futureList = new ArrayList<>();
    }

    public void startSync() throws IOException, ExecutionException, InterruptedException {
        // syncing server data to local
        futureList.clear();
        for (Todo t : fetchDataFromServer()) {
            insertOrUpdate(t);
        }
        join(futureList).thenAccept(list -> {
            //TODO: then sync Local Data to Server
        }).exceptionally(e -> {
            throw new RuntimeException(e);
        });
    }


    public void insertOrUpdate(Todo todo) {
        Todo localData = dao.get(todo.serverId);
        if (localData == null) {
            dao.create(todo);
        } else if (localData.isDeleted) {
            CompletableFuture<Response> f = apiService.delete(localData.serverId);
            futureList.add(f);
        } else {
            // Conflict resolution
            resolveConflict(localData, todo);
        }
    }

    private void resolveConflict(Todo localData, Todo serverData) {
        // Last updated wins
        if (serverData.updatedAt().after(localData.updatedAt())) {
            dao.update(localData.getId(), serverData);
        } else if (serverData.updatedAt().before(localData.updatedAt())) {
            CompletableFuture<Response> f = apiService.update(serverData.getId(), localData);
            futureList.add(f);
        }
    }

    private List<Todo> fetchDataFromServer() throws ExecutionException, InterruptedException, IOException {
        Response response = apiService.fetchAll().get();
        if (response.body() == null) throw new IOException();

        return Json.parseList(response.body().string(), Todo.class);
    }

    public static CompletableFuture<List<Response>> join(List<CompletableFuture<Response>> futures) {
        CompletableFuture<Void> cfv = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return cfv.thenApply(__ -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }
}
