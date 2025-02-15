package com.standalone.todos;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.standalone.core.App;
import com.standalone.core.ext.ApiService;
import com.standalone.core.utils.Json;
import com.standalone.todos.local.todos.Todo;
import com.standalone.todos.local.todos.TodoDao;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SyncManager implements Callback {
    private final TodoDao dao;
    private final ApiService<Todo> apiService;

    public SyncManager() {
        this.dao = new TodoDao();
        this.apiService = new ApiService<>();
    }

    public void startSync() throws IOException, ExecutionException, InterruptedException {
        // syncing server to local
        for (Todo t : fetchDataFromServer()) {
            insertOrUpdate(t);
        }
    }



    public void insertOrUpdate(Todo todo) throws JsonProcessingException {
        Todo localData = dao.get(todo.getId());
        if (localData == null) {
            dao.create(todo);
        } else {
            // Conflict resolution
            resolveConflict(localData, todo);
        }
    }

    private void resolveConflict(Todo localData, Todo serverData) throws JsonProcessingException {
        // Last updated wins
        if (serverData.updatedAt().after(localData.updatedAt())) {
            dao.update(localData.getId(), serverData);
        } else if (serverData.updatedAt().before(localData.updatedAt())) {
            apiService.set(this).update(serverData.getId(), localData);
        }
    }

    private List<Todo> fetchDataFromServer() throws ExecutionException, InterruptedException, IOException {
        Response response = apiService.fetchAll().get();
        if (response.body() == null) throw new IOException();

        return Json.parseList(response.body().string(), Todo.class);
    }
}
