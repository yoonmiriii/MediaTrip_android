package com.hani.mediaspot;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hani.mediaspot.adapter.SpotListAdapter;
import com.hani.mediaspot.api.FilterApi;
import com.hani.mediaspot.api.NetworkClient;
import com.hani.mediaspot.model.FilterRes;
import com.hani.mediaspot.model.Spot;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SpotListActivity extends AppCompatActivity {

    boolean isLoading = false;
    
    private EditText editKeyword;
    private TextView txtViewResult;
    private ImageButton btnSearch;
    private Button btnAll, btnTransport, btnPlayground, btnShop, btnStay,btnRestaurant,btnCafe;

    private FloatingActionButton fab;

    private RecyclerView recyclerViewSearchList;
    private SpotListAdapter spotListAdapter;
    private List<Spot> spotList = new ArrayList<>();
    private String currentCity;
    private String currentRegion;
    private String currentKeyword;
    private String currentLocationType;

    private int currentPage;
    private static final int pageSize = 10;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_list);

        editKeyword = findViewById(R.id.editKeyword);
        btnSearch = findViewById(R.id.btnSearch);
        txtViewResult = findViewById(R.id.txtViewResult);

        btnAll = findViewById(R.id.btnAll);
        btnTransport = findViewById(R.id.btnTransport);
        btnPlayground = findViewById(R.id.btnPlayground);
        btnShop = findViewById(R.id.btnShop);
        btnStay = findViewById(R.id.btnStay);
        btnRestaurant = findViewById(R.id.btnRestaurant);
        btnCafe = findViewById(R.id.btnCafe);

        fab = findViewById(R.id.fab);

        recyclerViewSearchList = findViewById(R.id.recyclerViewSearchList);
        recyclerViewSearchList.setLayoutManager(new LinearLayoutManager(this));
        spotListAdapter = new SpotListAdapter(spotList,currentCity,currentRegion);
        recyclerViewSearchList.setAdapter(spotListAdapter);

        // Intent로부터 city와 region 받아오기
        currentCity = "";
        currentRegion = "";

        currentCity = getIntent().getStringExtra("city");
        currentRegion = getIntent().getStringExtra("region");

        currentKeyword = "";
        currentLocationType = null;
        loadSpots(currentCity, currentRegion, currentKeyword, currentLocationType);

        setButtonBackgroundColor(btnAll);

        btnAll.setOnClickListener(v -> {
            currentLocationType = null;
            currentPage = 0;
            performActionBasedOnKeyword(currentCity, currentRegion, currentKeyword, currentLocationType);
            setButtonBackgroundColor(btnAll);
        });

        btnTransport.setOnClickListener(v -> {
            currentLocationType = "교통시설";
            currentPage = 0;
            performActionBasedOnKeyword(currentCity, currentRegion, currentKeyword, currentLocationType);
            setButtonBackgroundColor(btnTransport);
        });

        btnPlayground.setOnClickListener(v -> {
            currentLocationType = "문화 시설";
            currentPage = 0;
            performActionBasedOnKeyword(currentCity, currentRegion, currentKeyword, currentLocationType);
            setButtonBackgroundColor(btnPlayground);
        });

        btnShop.setOnClickListener(v -> {
            currentLocationType = "상점";
            currentPage = 0;
            performActionBasedOnKeyword(currentCity, currentRegion, currentKeyword, currentLocationType);
            setButtonBackgroundColor(btnShop);
        });

        btnStay.setOnClickListener(v -> {
            currentLocationType = "숙박";
            currentPage = 0;
            performActionBasedOnKeyword(currentCity, currentRegion, currentKeyword, currentLocationType);
            setButtonBackgroundColor(btnStay);
        });

        btnRestaurant.setOnClickListener(v -> {
            currentLocationType = "식당";
            currentPage = 0;
            performActionBasedOnKeyword(currentCity, currentRegion, currentKeyword, currentLocationType);
            setButtonBackgroundColor(btnRestaurant);
        });

        btnCafe.setOnClickListener(v -> {
            currentLocationType = "카페";
            currentPage = 0;
            performActionBasedOnKeyword(currentCity, currentRegion, currentKeyword, currentLocationType);
            setButtonBackgroundColor(btnCafe);
        });

        btnSearch.setOnClickListener(v -> {
            currentKeyword = editKeyword.getText().toString().trim();
            currentPage = 0;
            searchSpot(currentCity, currentRegion, currentKeyword, currentLocationType);
        });

        fab.setOnClickListener(v -> {
            if (!spotList.isEmpty()) {
                Intent intent = new Intent(SpotListActivity.this, MapActivity.class);
                ArrayList<Spot> spots = new ArrayList<>(spotList);
                intent.putExtra("spots", spots);
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
                        performActionBasedOnKeyword( currentCity, currentRegion, currentKeyword, currentLocationType);
                    }
                }
            }
        });
    }

    private void performActionBasedOnKeyword(String city, String region, String keyword, String locationType) {
        if (keyword != null && !keyword.isEmpty()) {
            searchSpot(city, region, keyword, locationType);
        } else {
            loadSpots(city, region,keyword,locationType);
        }
    }

    private void searchSpot(String city, String region, String keyword, String locationType) {
        isLoading = true;
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        FilterApi filterApi = retrofit.create(FilterApi.class);
        Call<FilterRes> call = filterApi.search(currentPage, pageSize, "", "", "", currentLocationType, currentCity, currentRegion, "", currentKeyword);

        Log.i("SPOT LIST", "city: " + currentCity);
        Log.i("SPOT LIST", "region: " + currentRegion);
        Log.i("SPOT LIST", "keyword: " + currentKeyword);
        Log.i("SPOT LIST", "locationType: " + currentLocationType);
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

    private void loadSpots(String city, String region, String keyword, @Nullable String locationType) {
        isLoading = true;
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        FilterApi filterApi = retrofit.create(FilterApi.class);
        Call<FilterRes> call = filterApi.filter(currentPage, pageSize, "", "", "", currentLocationType, currentCity, currentRegion, "","");

        Log.i("SPOT LIST", "city: " + city);
        Log.i("SPOT LIST", "region: " + region);
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

    private void setButtonBackgroundColor(Button selectedButton) {
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
}