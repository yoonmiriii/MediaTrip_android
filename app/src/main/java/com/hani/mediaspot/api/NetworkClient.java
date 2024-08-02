package com.hani.mediaspot.api;

import android.content.Context;

import com.hani.mediaspot.config.Config;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {

    public static Retrofit retrofit;

    public static Retrofit getRetrofitClient(Context context){
        if(retrofit == null){
            // 통신 로그 확인할때 필요한 코드
            HttpLoggingInterceptor loggingInterceptor =
                    new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


            // 네트워크 연결관련 코드

            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                    .cache(new Cache(context.getCacheDir(), 10 * 1024 * 1024)) // 10 MB Cache
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .addInterceptor(loggingInterceptor);

            // AuthInterceptor에 Context 전달
            httpClientBuilder.addInterceptor(new AuthInterceptor(context));

            OkHttpClient httpClient = httpClientBuilder.build();

            // 네트워크로 데이터를 보내고 받는
            // 레트로핏 라이브러리 관련 코드
            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.DOMAIN)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}


