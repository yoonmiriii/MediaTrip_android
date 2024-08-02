package com.hani.mediaspot.api;

import com.hani.mediaspot.model.FilterRes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FilterApi {

    // 지역분류 API
    @GET("/filter/location")
    Call<FilterRes> filterLocation(@Query ("offset") int offset,
                                   @Query ("limit") int limit,
                                   @Query ("city") String city);


    // 분류 API
    @GET("/filter")
    Call<FilterRes> filter(@Query ("offset") int offset,
                           @Query ("limit") int limit,
                           @Query ("mediaType") String mediaType,
                           @Query ("title") String title,
                           @Query ("location") String location,
                           @Query ("locationType") String locationType,
                           @Query ("city") String city,
                           @Query ("region") String region,
                           @Query ("address") String address,
                           @Query ("query") String query);

    // 검색 API
    @GET("/filter/search2")
    Call<FilterRes> search (@Query ("offset") int offset,
                           @Query ("limit") int limit,
                           @Query ("mediaType") String mediaType,
                           @Query ("title") String title,
                           @Query ("location") String location,
                           @Query ("locationType") String locationType,
                           @Query ("city") String city,
                           @Query ("region") String region,
                           @Query ("address") String address,
                           @Query ("query") String query);

    @GET("/location/hot")
    Call<FilterRes> getHotLocation(@Query ("offset") int offset,
                                   @Query ("limit") int limit,
                                   @Query ("locationType") String locationType,
                                   @Query ("query") String keyword);

    @GET("/location/distance")
    Call<FilterRes> getdistanceLocation(@Query ("offset") int offset,
                                        @Query ("limit") int limit,
                                        @Query ("latitude") double latitude,
                                        @Query ("longitude") double longitude,
                                        @Query ("locationType") String locationType,
                                        @Query ("query") String keyword);

}
