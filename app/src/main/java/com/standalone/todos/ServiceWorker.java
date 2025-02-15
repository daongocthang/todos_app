package com.standalone.todos;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.standalone.core.adapter.utils.Json;
import com.standalone.core.ext.ApiService;
import com.standalone.core.adapter.utils.DateUtil;
import com.standalone.todos.local.todos.Todo;
import com.standalone.todos.local.todos.TodoDao;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ServiceWorker extends Worker implements Callback {
    private static final String DATE_FORMAT = "YYYY-MM-DD HH:mm:ss";
    private final TodoDao dao;
    private final ApiService<Todo> apiService;

    public ServiceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.dao = new TodoDao();
        this.apiService = new ApiService<>();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            final DataEvent dataEvent = new DataEvent();

            List<Todo> todoList = fetchDataFromServer();
            for (Todo remoteItem : todoList) {
                String s = String.valueOf(remoteItem.getId());
                if (dataEvent.getAllByTag(DataEvent.DELETED).contains(s)) {
                    apiService.set(this).delete(remoteItem.getId());
                } else {
                    insertOrUpdate(remoteItem);
                }
            }

            for (String id : dataEvent.getAllByTag(DataEvent.CREATED)) {
                apiService.insert(dao.get(Long.parseLong(id)));
            }

            dataEvent.dispose();
            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }



    public void insertOrUpdate(Todo item) throws JsonProcessingException {
        Todo existingItem = dao.get(item.getId());
        if (existingItem == null) {
            dao.create(item);
        } else {
            // Conflict resolution
            resolveConflict(existingItem, item);
        }
    }

    private void resolveConflict(Todo localItem, Todo remoteItem) throws JsonProcessingException {
        // Last updated wins
        Date lastUpdatedLocal = DateUtil.parseTime(DATE_FORMAT, localItem.getUpdatedAt());
        Date lastUpdatedRemote = DateUtil.parseTime(DATE_FORMAT, remoteItem.getUpdatedAt());
        if (lastUpdatedRemote.getTime() > lastUpdatedLocal.getTime()) {
            dao.update(localItem.getId(), remoteItem);
        } else if (lastUpdatedRemote.getTime() < lastUpdatedLocal.getTime()) {
            apiService.update(remoteItem.getId(), localItem);
        }
    }

    private List<Todo> fetchDataFromServer() throws ExecutionException, InterruptedException, IOException {
        Response response = apiService.fetchAll().get();
        if (response.body() == null) throw new IOException();

        return Json.parseList(response.body().string(), Todo.class);
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        ApiService.raise(e);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) {
        if (!response.isSuccessful()) ApiService.raise(new IOException());
    }
}
