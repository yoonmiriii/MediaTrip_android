package com.hani.mediaspot.api;


import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.hani.mediaspot.config.Config;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;
    private String token;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    public AuthInterceptor(String token) {
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        // 토큰이 있을 경우에만 Authorization 헤더 추가
        SharedPreferences sp = context.getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");

        if (!token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }
}
