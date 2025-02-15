package com.standalone.core.ext.jwt;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// Require Internet permission
public class AuthClient {
    private final String tokenUrl;

    private final OkHttpClient client;

    private String username;
    private String password;

    public AuthClient(String tokenUrl, long callTimeoutMillis) {
        this.client = new OkHttpClient.Builder()
                .callTimeout(callTimeoutMillis, TimeUnit.MILLISECONDS)
                .build();
        this.tokenUrl = tokenUrl;
        this.username = "admin";
        this.password = "admin";
    }

    public AuthClient(String tokenUrl) {
        this(tokenUrl, 1000);
    }

    public void setRequestForm(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public void Authorize(TokenCallback callback) {
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(tokenUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() == null) throw new IOException("Error during get body");

                @SuppressWarnings("unchecked")
                Map<String, Object> map = new ObjectMapper().readValue(response.body().byteStream(), HashMap.class);

                if (!map.containsKey("access_token"))
                    throw new IOException("Access token not found");

                callback.onSuccess((String) map.get("access_token"));
            }
        });
    }

    public interface TokenCallback {
        void onError(IOException e);

        void onSuccess(String token);
    }
}
