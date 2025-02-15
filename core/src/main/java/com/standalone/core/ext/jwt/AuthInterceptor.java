package com.standalone.core.ext.jwt;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final String token;

    public AuthInterceptor(String token) {
        this.token = token;
    }

    private boolean isEmptyToken() {
        return token == null || token.isEmpty();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalReq = chain.request();
        Request authReq = originalReq;
        if (!originalReq.url().toString().contains("/auth") && !isEmptyToken()) {
            authReq = originalReq.newBuilder()
                    .header("Authentication", "Bearer " + token)
                    .build();
        }

        return chain.proceed(authReq);
    }
}
