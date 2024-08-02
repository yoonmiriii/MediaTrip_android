package com.hani.mediaspot;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class RegisterActivity extends AppCompatActivity {

    EditText editName;
    EditText editEmail;
    EditText editPassword;

    RadioGroup radioGroupGender;
    RadioButton btnMale;
    RadioButton btnFemale;

    Button btnRegister;
    Button btnLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnMale = findViewById(R.id.btnMale);
        btnFemale = findViewById(R.id.btnFemale);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        radioGroupGender = findViewById(R.id.radioGroupGender);


        // 회원가입 버튼 클릭 리스너 설정
        View.OnClickListener registerClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();

                String name = editName.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;



                // 입력값 유효성 검사
                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    dismissProgress();
                    // 필수 입력 필드가 비어 있는 경우 처리
                    Toast.makeText(RegisterActivity.this,
                            "필수 사항입니다, 모두 입력해주세요.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // 이름 글자수 제한
                if( name.length() < 2 || name.length() > 8){
                    dismissProgress();
                    Toast.makeText(RegisterActivity.this,
                            "이름은 2글자 이상, 8글자 이하로 입력하세요",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pattern.matcher(email).matches()){
                    dismissProgress();
                    // 이메일 형식 유효성 통과
                } else {
                    // 이메일 형식에 안 맞음
                    dismissProgress();
                    Toast.makeText(RegisterActivity.this,
                            "이메일 형식이 맞지않습니다, 다시 입력해주세요",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // 패스워드 글자수 제한
                if ( password.length() < 4 || password.length() > 12){
                    dismissProgress();
                    Toast.makeText(RegisterActivity.this,
                            "비밀번호는 4자 이상, 12자 이하로 입력하세요",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // 성별 선택 확인
                int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
                if (selectedGenderId == -1) {
                    dismissProgress();
                    // 성별이 선택되지 않은 경우 처리
                    Toast.makeText(RegisterActivity.this,
                            "성별을 선택해주세요.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // 선택된 라디오버튼의 텍스트 가져오기
                RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
                String genderString = selectedGenderRadioButton.getText().toString().trim();

                // DB에 저장할 성별 boolean 값으로 변환
                boolean gender;
                if (genderString.equals("남성")) {
                    gender = true;  // 남성이면 true
                } else {
                    gender = false; // 여성이면 false
                }

                // 회원가입 API 호출
                Retrofit retrofit = NetworkClient.getRetrofitClient(RegisterActivity.this);
                UserApi api = retrofit.create(UserApi.class);
                User user = new User(name, email, password, gender);

                Call<UserRes> call = api.register(user);
                call.enqueue(new Callback<UserRes>() {
                    @Override
                    public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                        dismissProgress();

                        if (response.isSuccessful()) {
                            UserRes userRes = response.body();
                            SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token", userRes.accessToken);

                            editor.commit();

                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);


                        }
                    }

                    @Override
                    public void onFailure(Call<UserRes> call, Throwable throwable) {
                        dismissProgress();
                        return;
                    }
                });


            }
        };

        btnRegister.setOnClickListener(registerClickListener);

        // 엔터 키 리스너 설정
        editPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    btnRegister.performClick();  // 회원가입 버튼 클릭 동작 수행
                    return true;
                }
                return false;
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
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
        dialog.dismiss();
    }
}