package com.hani.mediaspot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // 일정 시간이 지나면 MainActivity로 이동
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);

                // 이전 키를 눌렀을 때 스플래스 스크린 화면으로 이동을 방지하기 위해
                // 이동한 다음 사용안함으로 finish 처리
                finish();
            }
        }, 1500); // 시간 2초 이후 실행
    }
}
