package com.hani.mediaspot.api;

import com.hani.mediaspot.model.User;
import com.hani.mediaspot.model.UserRes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface UserApi {

    // 로그인 API
    @POST("/user/login")
    Call<UserRes> login(@Body User user);

    // 회원가입 API
    @POST("/user/register")
    Call<UserRes> register(@Body User user);

//    // 회원정보수정 API
//    @PUT ("/user")
//    Call<UserRes> updateProfile(@Header("Authorization") String token,
//                                @Body User user);

    // 로그아웃 API
    @DELETE("/user/logout")
    Call<UserRes> logout();

//    // 회원 탈퇴 API
//    @DELETE("/user/delete")
//    Call<UserRes> deleteAccount(@Header("Authorization") String token);


}
