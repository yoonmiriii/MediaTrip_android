package com.hani.mediaspot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hani.mediaspot.adapter.SpotListAdapter;
import com.hani.mediaspot.api.FilterApi;
import com.hani.mediaspot.api.LikeApi;
import com.hani.mediaspot.api.NetworkClient;
import com.hani.mediaspot.model.FilterRes;
import com.hani.mediaspot.model.Spot;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LbsFragment extends Fragment {

    boolean isLoading = false;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location lastKnownLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private EditText editKeyword;
    private TextView txtViewResult;
    private ImageButton btnSearch;
    private Button btnAll, btnTransport, btnPlayground, btnShop, btnStay, btnRestaurant, btnCafe;

    private FloatingActionButton fab;

    private RecyclerView recyclerViewSearchList;
    private SpotListAdapter spotListAdapter;
    private List<Spot> spotList = new ArrayList<>();

    private String keyword;
    private String locationType;

    double latitude;
    double longitude;

    private int currentPage;
    private static final int pageSize = 10;

    public static LbsFragment newInstance() {
        return new LbsFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                lastKnownLocation = locationResult.getLastLocation();
            }
        };

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lbs, container, false);

        editKeyword = view.findViewById(R.id.editKeyword);
        btnSearch = view.findViewById(R.id.btnSearch);
        txtViewResult = view.findViewById(R.id.txtViewResult);

        btnAll = view.findViewById(R.id.btnAll);
        btnTransport = view.findViewById(R.id.btnTransport);
        btnPlayground = view.findViewById(R.id.btnPlayground);
        btnShop = view.findViewById(R.id.btnShop);
        btnStay = view.findViewById(R.id.btnStay);
        btnRestaurant = view.findViewById(R.id.btnRestaurant);
        btnCafe = view.findViewById(R.id.btnCafe);

        fab = view.findViewById(R.id.fab);

        recyclerViewSearchList = view.findViewById(R.id.recyclerViewSearchList);
        recyclerViewSearchList.setLayoutManager(new LinearLayoutManager(getContext()));
        spotListAdapter = new SpotListAdapter(spotList);
        recyclerViewSearchList.setAdapter(spotListAdapter);

        keyword = "";
        locationType = null;


//        performActionBasedOnKeyword("", null);
        setButtonBackgroundColor(btnAll);


        btnAll.setOnClickListener(v -> {

            currentPage = 0;
            performActionBasedOnKeyword(keyword, null);
            setButtonBackgroundColor(btnAll);
        });

        btnTransport.setOnClickListener(v -> {
            locationType = "교통시설";
            currentPage = 0;
            performActionBasedOnKeyword(keyword, locationType);
            setButtonBackgroundColor(btnTransport);
        });

        btnPlayground.setOnClickListener(v -> {
            locationType = "문화 시설";
            currentPage = 0;
            performActionBasedOnKeyword(keyword, locationType);
            setButtonBackgroundColor(btnPlayground);
        });

        btnShop.setOnClickListener(v -> {
            locationType = "상점";
            currentPage = 0;
            performActionBasedOnKeyword(keyword, locationType);
            setButtonBackgroundColor(btnShop);
        });

        btnStay.setOnClickListener(v -> {
            locationType = "숙박";
            currentPage = 0;
            performActionBasedOnKeyword(keyword, locationType);
            setButtonBackgroundColor(btnStay);
        });

        btnRestaurant.setOnClickListener(v -> {
            locationType = "식당";
            currentPage = 0;
            performActionBasedOnKeyword(keyword, locationType);
            setButtonBackgroundColor(btnRestaurant);
        });

        btnCafe.setOnClickListener(v -> {
            locationType = "카페";
            currentPage = 0;
            performActionBasedOnKeyword(keyword, locationType);
            setButtonBackgroundColor(btnCafe);
        });

        btnSearch.setOnClickListener(v -> {
            keyword = editKeyword.getText().toString().trim();
            currentPage = 0;
            performActionBasedOnKeyword(keyword, null);
        });

        fab.setOnClickListener(v -> {
            if (!spotList.isEmpty()) {
                Intent intent = new Intent(getContext(), MapActivity.class);
                ArrayList<Spot> spots = new ArrayList<>(spotList);
                intent.putExtra("spots", spots);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                Log.i("MAIN LATLNG", "lat: "+ latitude+ "lng: "+ longitude);
                startActivity(intent);
            }
        });

        recyclerViewSearchList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                    if (!isLoading && lastVisibleItemPosition >= totalItemCount - 2) {
                        currentPage += pageSize;

                        latitude = lastKnownLocation.getLatitude();
                        longitude = lastKnownLocation.getLongitude();

                        performActionBasedOnKeyword( keyword, locationType);
                    }
                }
            }
        });

//        startLocationUpdates();
        requestLocationAndCallDistanceLocation();

        return view;
    }

    private void performActionBasedOnKeyword(String keyword, String locationType) {
        if (keyword != null && !keyword.isEmpty()) {
            searchSpot(keyword, locationType);
        } else {
            getdistanceLocation(latitude, longitude, locationType);
        }
    }

    private void searchSpot(String keyword, String locationType) {
        isLoading = true;
        Retrofit retrofit = NetworkClient.getRetrofitClient(getContext());
        FilterApi filterApi = retrofit.create(FilterApi.class);
        Call<FilterRes> call = filterApi.search(currentPage, pageSize, "", "", "", locationType, "", "", "", keyword);

        Log.i("SPOT LIST", "keyword: " + keyword);
        Log.i("SPOT LIST", "locationType: " + locationType);
        call.enqueue(new Callback<FilterRes>() {
            @Override
            public void onResponse(Call<FilterRes> call, Response<FilterRes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isLoading = false;
                    if (currentPage == 0) {
                        spotList.clear();
                    }
                    spotList.addAll(response.body().getItems());
                    if (spotList.isEmpty()) {
                        txtViewResult.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.GONE);
                    } else {
                        txtViewResult.setVisibility(View.GONE);
                        fab.setVisibility(View.VISIBLE);
                    }
                    spotListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<FilterRes> call, Throwable t) {
                isLoading = false;
                Log.e("SpotListActivity", "Error loading spots", t);
            }
        });
    }

    private void getdistanceLocation(double latitude, double longitude ,String locationType) {
        isLoading = true;
        Log.i("LBS", "latitude : "+ latitude + "Longitude: "+ longitude);
        Retrofit retrofit = NetworkClient.getRetrofitClient(getContext());
        FilterApi filterApi = retrofit.create(FilterApi.class);
        Call<FilterRes> call = filterApi.getdistanceLocation(currentPage, pageSize, latitude, longitude, locationType,"");

        call.enqueue(new Callback<FilterRes>() {
            @Override
            public void onResponse(Call<FilterRes> call, Response<FilterRes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isLoading = false;
                    if (currentPage == 0) {
                        spotList.clear();
                    }
                    spotList.addAll(response.body().getItems());
                    if (spotList.isEmpty()) {
                        txtViewResult.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.GONE);
                    } else {
                        txtViewResult.setVisibility(View.GONE);
                        fab.setVisibility(View.VISIBLE);
                    }
                    spotListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<FilterRes> call, Throwable t) {
                isLoading = false;
                Log.e("SpotListActivity", "Error loading spots", t);
            }
        });
    }
    private void setButtonBackgroundColor (Button selectedButton){
        btnAll.setBackgroundColor(Color.TRANSPARENT);
        btnTransport.setBackgroundColor(Color.TRANSPARENT);
        btnPlayground.setBackgroundColor(Color.TRANSPARENT);
        btnShop.setBackgroundColor(Color.TRANSPARENT);
        btnStay.setBackgroundColor(Color.TRANSPARENT);
        btnRestaurant.setBackgroundColor(Color.TRANSPARENT);
        btnCafe.setBackgroundColor(Color.TRANSPARENT);

        btnAll.setTextColor(Color.BLACK);
        btnTransport.setTextColor(Color.BLACK);
        btnPlayground.setTextColor(Color.BLACK);
        btnShop.setTextColor(Color.BLACK);
        btnStay.setTextColor(Color.BLACK);
        btnRestaurant.setTextColor(Color.BLACK);
        btnCafe.setTextColor(Color.BLACK);

        selectedButton.setBackgroundColor(Color.BLACK);
        selectedButton.setTextColor(Color.WHITE);
    }

//    private void startLocationUpdates() {
//        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
//        } else {
//            ActivityCompat.requestPermissions(getActivity(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//        }
//    }

    private void requestLocationAndCallDistanceLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lastKnownLocation = location;
                                latitude = lastKnownLocation.getLatitude();
                                longitude = lastKnownLocation.getLongitude();
                                getdistanceLocation(latitude, longitude, locationType);
                            } else {
                                Toast.makeText(getContext(), "Unable to get current location.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationAndCallDistanceLocation();
            } else {
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove location updates when the fragment is destroyed to prevent memory leaks
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}