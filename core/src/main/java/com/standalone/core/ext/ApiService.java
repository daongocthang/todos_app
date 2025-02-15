package com.standalone.core.ext;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.standalone.core.App;
import com.standalone.core.adapter.utils.Json;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService<T> {
    public static final String BASE_URL = App.loadEnv().getProperty("BASE_URL");
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;
    private Callback callback;

    public ApiService() {
        this.client = new OkHttpClient.Builder()
                .callTimeout(1, TimeUnit.SECONDS)
                .build();
    }

    public ApiService<T> set(Callback callback) {
        this.callback = callback;
        return this;
    }

    public void insert(T t) throws JsonProcessingException {
        RequestBody body = RequestBody.create(Json.stringify(t), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build();

        assert callback != null;
        client.newCall(request).enqueue(callback);
    }

    public Future<Response> fetchAll() {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .build();
        return call(request);
    }

    public Future<Response> fetchById(long id) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + String.valueOf(id))
                .build();
        return call(request);
    }

    public void update(long id, T t) throws JsonProcessingException {
        RequestBody body = RequestBody.create(Json.stringify(t), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + String.valueOf(id))
                .put(body)
                .build();

        assert callback != null;
        client.newCall(request).enqueue(callback);
    }

    public void delete(long id) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + String.valueOf(id))
                .delete()
                .build();
        assert callback != null;
        client.newCall(request).enqueue(callback);
    }

    public static void raise(Throwable e) {
        throw new RuntimeException(e);
    }

    private CompletableFuture<Response> call(Request request) {
        ResponseFuture callback = new ResponseFuture();
        client.newCall(request).enqueue(callback);
        return callback.future;
    }

    public static class ResponseFuture implements Callback {
        CompletableFuture<Response> future = new CompletableFuture<>();

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            future.completeExceptionally(e);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            future.complete(response);
        }
    }
}
