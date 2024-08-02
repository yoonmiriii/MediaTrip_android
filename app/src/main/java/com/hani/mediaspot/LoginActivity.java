package com.hani.mediaspot;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


import com.hani.mediaspot.api.NetworkClient;
import com.hani.mediaspot.api.UserApi;
import com.hani.mediaspot.config.Config;
import com.hani.mediaspot.model.User;
import com.hani.mediaspot.model.UserRes;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    EditText editEmail;
    EditText editPassword;

    Button btnLogin;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // 로그인 버튼 클릭 리스너 설정
        View.OnClickListener loginClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();

                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                if(email.isEmpty() || password.isEmpty()){
                    dismissProgress();
                    Toast.makeText(LoginActivity.this,
                            "필수 항목입니다, 모두 입력해주세요",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Pattern pattern = Patterns.EMAIL_ADDRESS;
                if(pattern.matcher(email).matches() == false){
                    Toast.makeText(LoginActivity.this,
                            "이메일 형식이 올바르지않습니다, 다시 입력해주세요",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Retrofit retrofit = NetworkClient.getRetrofitClient(LoginActivity.this);
                UserApi api = retrofit.create(UserApi.class);
                User user = new User(email,password);

                Call<UserRes> call = api.login(user);
                call.enqueue(new Callback<UserRes>() {
                    @Override
                    public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                        dismissProgress();

                        if (response.isSuccessful()){
                            UserRes userRes = response.body();
                            SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token", userRes.accessToken);
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                            startActivity(intent);
                            Toast.makeText(LoginActivity.this,
                                    "로그인에 성공했습니다",
                                    Toast.LENGTH_SHORT).show();
                            finish();


                        }else {
                            Toast.makeText(LoginActivity.this,
                                    "이메일 혹은 패스워드가 일치하지않습니다, 다시 확인해주세요",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }

                    @Override
                    public void onFailure(Call<UserRes> call, Throwable t) {
                        dismissProgress();
                        Toast.makeText(LoginActivity.this,
                                "로그인 실패: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        };
        btnLogin.setOnClickListener(loginClickListener);

        // 엔터 키 리스너 설정
        editPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    btnLogin.performClick();  // 로그인 버튼 클릭 동작 수행
                    return true;
                }
                return false;
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }




    Dialog dialog;
    void showProgress(){
        dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(new ProgressBar(this));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    void dismissProgress(){
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}