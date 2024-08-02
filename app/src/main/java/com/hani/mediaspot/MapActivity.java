package com.hani.mediaspot;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Display;
import android.view.WindowManager;
import android.graphics.Point;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.hani.mediaspot.model.Spot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.MapView;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.LocationSource;


import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private ArrayList<Spot> spots;
    private PopupWindow popupWindow;
    private double latitude, longitude;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Location lastKnownLocation;
    private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private NaverMap naverMap;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        spots = (ArrayList<Spot>) getIntent().getSerializableExtra("spots");

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Intent에서 위도와 경도 가져오기
        Intent intent = getIntent();
        if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            latitude = intent.getDoubleExtra("latitude", 0.0);
            longitude = intent.getDoubleExtra("longitude", 0.0);
        }

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // LocationSource 설정
        naverMap.setLocationSource(locationSource);
        // 현재 위치 추적 설정
        naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);

        // 지도 클릭 시 팝업 창 닫기
        naverMap.setOnMapClickListener((coord, point) -> {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        });

        // 마커 설정 및 팝업 창 표시
        if (spots != null && !spots.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Spot spot : spots) {
                marker = new Marker();
                LatLng position = new LatLng(spot.getLatitude(), spot.getLongitude());
                marker.setPosition(position);
                marker.setMap(naverMap);

                builder.include(position);
                LatLngBounds bounds = builder.build();


                if (spots.size() > 1) {
                    CameraUpdate cameraUpdate = CameraUpdate.fitBounds(bounds, 300);
                    naverMap.moveCamera(cameraUpdate);

                    LatLng initialPosition = null;
                    if (latitude != 0.0 && longitude != 0.0) {
                        // Intent에서 받은 위도와 경도로 위치 설정
                        initialPosition = new LatLng(latitude, longitude);
                        cameraUpdate = CameraUpdate.scrollTo(initialPosition);
                        naverMap.moveCamera(cameraUpdate);
                        naverMap.moveCamera(CameraUpdate.zoomTo(13));
                    }
                } else {
                    LatLng singleMarkerLatLng = new LatLng(spots.get(0).getLatitude(), spots.get(0).getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdate.scrollTo(singleMarkerLatLng);
                    naverMap.moveCamera(cameraUpdate);
                    naverMap.moveCamera(CameraUpdate.zoomTo(10));

                    LatLng initialPosition = null;
                    if (latitude != 0.0 && longitude != 0.0) {
                        // Intent에서 받은 위도와 경도로 위치 설정
                        initialPosition = new LatLng(latitude, longitude);
                        cameraUpdate = CameraUpdate.scrollTo(initialPosition);
                        naverMap.moveCamera(cameraUpdate);
                        naverMap.moveCamera(CameraUpdate.zoomTo(10));
                    }
                }

                marker.setOnClickListener(overlay -> {
                    showPopup(spot, position);
                    return true;
                });
            }
        }
    }


    private void showPopup(Spot spot, LatLng position) {
        // Remove existing popupWindow if it exists
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        // Inflate the layout for the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_info, null);

        int popupWidth = 400;
        int popupHeight = 150;

        // Initialize PopupWindow with fixed dimensions
        popupWindow = new PopupWindow(popupView, dpToPx(popupWidth), dpToPx(popupHeight));

        // Populate the popup window with data
        ImageView imageView = popupView.findViewById(R.id.image);
        TextView txtLocation = popupView.findViewById(R.id.txtLocation);
        TextView txtLocationType = popupView.findViewById(R.id.txtLocationType);
        TextView txtAddress = popupView.findViewById(R.id.txtAddress);

        txtLocation.setText(spot.getLocation());
        txtLocationType.setText(spot.getLocationType());
        txtAddress.setText(spot.getAddress());

        if (spot.getImgUrl() != null && !spot.getImgUrl().isEmpty()) {
            Glide.with(this)
                    .load(spot.getImgUrl())
                    .placeholder(R.drawable.baseline_image_24)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.baseline_image_24);
        }

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int screenWidth = size.x;
        int screenHeight = size.y;

        // Calculate position for PopupWindow
        int xOffset = (screenWidth - popupWindow.getWidth()) / 2;
        int yOffset = screenHeight - popupWindow.getHeight() - 100;

        // Show PopupWindow at calculated position
        popupWindow.showAtLocation(mapView, android.view.Gravity.NO_GRAVITY, xOffset, yOffset);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        popupView.setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, DetailViewActivity.class);
            intent.putExtra("spot", spot);
            startActivity(intent);
            popupWindow.dismiss(); // Optionally dismiss the popup
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) {
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
}