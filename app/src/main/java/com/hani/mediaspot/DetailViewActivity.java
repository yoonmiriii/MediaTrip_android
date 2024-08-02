package com.hani.mediaspot;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hani.mediaspot.adapter.DetailAdapter;
import com.hani.mediaspot.api.FilterApi;
import com.hani.mediaspot.api.LikeApi;
import com.hani.mediaspot.api.NetworkClient;
import com.hani.mediaspot.config.Config;
import com.hani.mediaspot.model.FilterRes;
import com.hani.mediaspot.model.Res;
import com.hani.mediaspot.model.Spot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DetailViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ImageView imgView, imgViewFavorite;
    private TextView txtAddress, txtLocation, txtLocationType;
    private RecyclerView recyclerViewDetail;
    private DetailAdapter detailAdapter;
    private List<Spot> spotList = new ArrayList<>();
    private Spot currentSpot;
    private MapView mapView;
    private NaverMap naverMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        imgView = findViewById(R.id.imgView);
        imgViewFavorite = findViewById(R.id.imgViewFavorite);
        txtAddress = findViewById(R.id.txtAddress);
        txtLocation = findViewById(R.id.txtLocation);
        txtLocationType = findViewById(R.id.txtLocationType);
        recyclerViewDetail = findViewById(R.id.recyclerViewDetail);
        recyclerViewDetail.setLayoutManager(new LinearLayoutManager(this));

        detailAdapter = new DetailAdapter(spotList);
        recyclerViewDetail.setAdapter(detailAdapter);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 인텐트에서 데이터 받아오기
        Spot spot = (Spot) getIntent().getSerializableExtra("spot");

        if (spot != null) {
            String location = spot.getLocation();
            String locationType = spot.getLocationType();
            String address = spot.getAddress();
            double latitude = spot.getLatitude();
            double longitude = spot.getLongitude();

            txtLocation.setText(location);
            txtLocationType.setText(locationType);
            txtAddress.setText(address);

            // Retrofit을 사용하여 데이터를 가져오는 부분
            Retrofit retrofit = NetworkClient.getRetrofitClient(this);
            FilterApi filterApi = retrofit.create(FilterApi.class);
            Call<FilterRes> call = filterApi.filter(0, 10, null, null, null, locationType, null, null, address, null);
            call.enqueue(new Callback<FilterRes>() {
                @Override
                public void onResponse(Call<FilterRes> call, Response<FilterRes> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        spotList.clear();
                        spotList.addAll(response.body().getItems());
                        Log.i("DETAIL", spotList.toString());
                        detailAdapter.notifyDataSetChanged();

                        if (!spotList.isEmpty()) {
                            currentSpot = spotList.get(0);
                            String imgUrl = currentSpot.getImgUrl();
                            if (imgUrl != null) {
                                Glide.with(DetailViewActivity.this)
                                        .load(imgUrl)
                                        .into(imgView);
                            }

                            updateFavoriteIcon(currentSpot.getIsLike());
                        }
                    } else {
                        Log.i("DETAIL", "RESPONSE FAIL");
                    }
                }

                @Override
                public void onFailure(Call<FilterRes> call, Throwable t) {
                    Log.e("DetailViewActivity", "API CALL FAILED", t);
                }
            });

            imgViewFavorite.setOnClickListener(v -> {
                SharedPreferences sp = getSharedPreferences(Config.SP_NAME, Context.MODE_PRIVATE);
                boolean isLoggedIn = sp.getBoolean("isLoggedIn", false);

                String token = sp.getString("token", "");
                Log.i("TOKEN", "token: " + token);
                Log.i("LOGGEDIN", "LoggedIn: " + isLoggedIn);

                if (!isLoggedIn) {
                    // 로그인하지 않은 경우 로그인 액티비티로 이동
                    new AlertDialog.Builder(DetailViewActivity.this)
                            .setMessage("로그인하시겠습니까?")
                            .setPositiveButton("Yes", (dialog, id) -> {
                                Intent intent = new Intent(DetailViewActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                            .create()
                            .show();
                } else {
                    // 로그인된 경우 좋아요 API 호출
                    if (currentSpot != null) {
                        if (currentSpot.getIsLike() == 1) {
                            likeCancel(currentSpot.getId());
                        } else {
                            like(currentSpot.getId());
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        Spot spot = (Spot) getIntent().getSerializableExtra("spot");
        if (spot != null) {
            double latitude = spot.getLatitude();
            double longitude = spot.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            Marker marker = new Marker();
            marker.setPosition(latLng);
            marker.setMap(naverMap);

            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(latLng);
            naverMap.moveCamera(cameraUpdate);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void updateFavoriteIcon(int isLike) {
        if (isLike == 1) {
            imgViewFavorite.setImageResource(R.drawable.baseline_favorite_fill_24);
        } else {
            imgViewFavorite.setImageResource(R.drawable.baseline_favorite_24);
        }
    }

    private void like(int mediaId) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        LikeApi likeApi = retrofit.create(LikeApi.class);
        Call<Res> call = likeApi.like(mediaId);
        call.enqueue(new Callback<Res>() {
            @Override
            public void onResponse(Call<Res> call, Response<Res> response) {
                if (response.isSuccessful()) {
                    // Update UI
                    updateFavoriteIcon(1);
                    updateSpotList(mediaId, 1);
                } else {
                    Log.i("DETAIL", "LIKE API CALL FAILED");
                }
            }

            @Override
            public void onFailure(Call<Res> call, Throwable t) {
                Log.e("DetailViewActivity", "LIKE API CALL FAILED", t);
            }
        });
    }

    private void likeCancel(int mediaId) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        LikeApi likeApi = retrofit.create(LikeApi.class);
        Call<Res> call = likeApi.likeCancel(mediaId);
        call.enqueue(new Callback<Res>() {
            @Override
            public void onResponse(Call<Res> call, Response<Res> response) {
                if (response.isSuccessful()) {
                    // Update UI
                    updateFavoriteIcon(0);
                    updateSpotList(mediaId, 0);
                } else {
                    Log.i("DETAIL", "LIKE CANCEL API CALL FAILED");
                }
            }

            @Override
            public void onFailure(Call<Res> call, Throwable t) {
                Log.e("DetailViewActivity", "LIKE CANCEL API CALL FAILED", t);
            }
        });
    }

    private void updateSpotList(int mediaId, int isLike) {
        for (Spot spot : spotList) {
            if (spot.getId() == mediaId) {
                spot.setIsLike(isLike);
                break;
            }
        }
        detailAdapter.notifyDataSetChanged();
    }
}
