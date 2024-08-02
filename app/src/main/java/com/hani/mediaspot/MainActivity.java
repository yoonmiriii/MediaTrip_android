package com.hani.mediaspot;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.hani.mediaspot.api.NetworkClient;
import com.hani.mediaspot.api.UserApi;
import com.hani.mediaspot.config.Config;
import com.hani.mediaspot.model.UserRes;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    Fragment homeFragment;
    Fragment lbsFragment;
    Fragment locationFragment;
    Fragment favoriteFragment;


    private SharedPreferences sp;
    private boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreferences에서 로그인 상태 가져오기
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        String token= sp.getString("token","");
        isLoggedIn = sp.getBoolean("isLoggedIn", false); // 기본값은 false로 설정
        Log.i("MAIN TOKEN", token );

        // 액션바 설정
        setupActionBar();


        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        homeFragment = new HomeFragment();
        lbsFragment = new LbsFragment();
        locationFragment = new AddressFragment();
        favoriteFragment = new FavoriteFragment();


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                Fragment fragment = null;

                if(itemId == R.id.homeFragment){

                    fragment = homeFragment;

                }else if (itemId == R.id.lbsFragment){

                    fragment = lbsFragment;

                }else if (itemId == R.id.locationFragment){

                    fragment = locationFragment;

                }else if (itemId == R.id.favoriteFragment){

                    fragment = favoriteFragment;


                }

                return loadFragment(fragment);


            }
        });

    }
    boolean loadFragment(Fragment fragment){
        if(fragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit();
            return true;
        }else{
            return false;
        }
    }

    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_login) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.menu_logout){
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("정말 로그아웃 하시겠습니까?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 사용자가 로그아웃을 확인하면 로그아웃 처리 진행
                        Logout();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 사용자가 취소를 선택한 경우 아무 작업도 하지 않음
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void Logout() {
        showProgress();

        // 실제 로그아웃 API 호출

        UserApi userApi = NetworkClient.getRetrofitClient(MainActivity.this).create(UserApi.class);
        Call<UserRes> call = userApi.logout();

        call.enqueue(new Callback<UserRes>() {
            @Override
            public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                if (response.isSuccessful()) {
                    dismissProgress();
                    // 로그아웃 성공 시 처리
                    // SharedPreferences에서 토큰 삭제
                    sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                    String token = sp.getString("token","");
                    Log.i("MAIN TOKEN", token);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.remove("token"); // 토큰 삭제
                    editor.remove("isLoggedIn"); // 로그인 상태 키 삭제
                    editor.apply();

                    // ActionBar 업데이트
                    updateLoginStatus(false);
                    Toast.makeText(MainActivity.this,
                            "로그아웃 되었습니다",
                            Toast.LENGTH_SHORT).show();
                    // 화면 새로고침 또는 재시작
                    finish(); // 현재 액티비티 종료
                    startActivity(getIntent()); // 현재 액티비티 다시 시작

                } else {
                    dismissProgress();
                    // 로그아웃 실패 시 처리
                    Toast.makeText(MainActivity.this,
                            "로그아웃 실패",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserRes> call, Throwable t) {
                // 네트워크 오류 처리
                dismissProgress();
                Toast.makeText(MainActivity.this,
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("RESUME MAIN", "Received onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        Log.i("RESUME MAIN",String.valueOf(requestCode));
        if (requestCode == 7001 && resultCode == RESULT_OK) {
            // SharedPreferences에 토큰 저장
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.apply();

            // UI 업데이트
            updateLoginStatus(true);

        }
    }

    private void updateLoginStatus(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
        // SharedPreferences에 로그인 상태 저장
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn).apply();
        // 액션바 업데이트
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // 메뉴 아이템 가시성 설정
        MenuItem login = menu.findItem(R.id.menu_login);
        MenuItem logout = menu.findItem(R.id.menu_logout);

        // 로그인 상태에 따라 메뉴 아이템 가시성 설정
        if (isLoggedIn == true) {
            login.setVisible(false);
            logout.setVisible(true);
        } else if (isLoggedIn == false){
            login.setVisible(true);
            logout.setVisible(false);
        }

        return true;
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

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        String token= sp.getString("token","");
        boolean isLoggedIn = sp.getBoolean("isLoggedIn", false);
        Log.i("RESUME MAIN", "isLoggedIn: " + isLoggedIn);
        Log.i("MAIN TOKEN", "token: " + token );
        updateLoginStatus(isLoggedIn);
        invalidateOptionsMenu();
    }

}