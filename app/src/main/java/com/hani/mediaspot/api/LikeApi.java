package com.hani.mediaspot.api;

import com.hani.mediaspot.model.FilterRes;
import com.hani.mediaspot.model.Res;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LikeApi {

    // 좋아요 하는 API
    @POST("/like/{mediaId}")
    Call<Res> like(@Path("mediaId") int mediaId);

    // 좋아요 취소 API
    @DELETE("/like/{mediaId}")
    Call<Res> likeCancel (@Path("mediaId") int mediaId);

    // 좋아요 목록 API
    @GET("/mylike")
    Call<FilterRes> likeList (@Query ("offset") int offset,
                              @Query ("limit") int limit,
                              @Query ("mediaType") String mediaType,
                              @Query ("title") String title,
                              @Query ("location") String location,
                              @Query ("locationType") String locationType,
                              @Query ("city") String city,
                              @Query ("region") String region,
                              @Query ("address") String address,
                              @Query ("query") String query);

    // 좋아요 목록 검색 API
    @GET("/mylike/search")
    Call<FilterRes> likeListSearch (@Query ("offset") int offset,
                                    @Query ("limit") int limit,
                                    @Query ("mediaType") String mediaType,
                                    @Query ("title") String title,
                                    @Query ("location") String location,
                                    @Query ("locationType") String locationType,
                                    @Query ("city") String city,
                                    @Query ("region") String region,
                                    @Query ("address") String address,
                                    @Query ("query") String query);

    
}
