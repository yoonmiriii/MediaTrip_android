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

public class LocationListActivity extends AppCompatActivity {

    boolean isLoading = false;
    private EditText editKeyword;
    private TextView txtViewResult;
    private ImageButton btnSearch;
    private Button btnAll, btnTransport, btnPlayground, btnShop, btnStay,btnRestaurant,btnCafe;

    private FloatingActionButton fab;
    
    private RecyclerView recyclerViewSearchList;
    private SpotListAdapter spotListAdapter;
    private List<Spot> spotList = new ArrayList<>();

    private String keyword;
    private String locationType;
    
    private int currentPage;
    private static final int pageSize = 10;
    

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        editKeyword = findViewById(R.id.editKeyword);
        btnSearch = findViewById(R.id.btnSearch);
        txtViewResult = findViewById(R.id.txtViewResult);

        fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        btnAll = findViewById(R.id.btnAll);
        btnTransport = findViewById(R.id.btnTransport);
        btnPlayground = findViewById(R.id.btnPlayground);
        btnShop = findViewById(R.id.btnShop);
        btnStay = findViewById(R.id.btnStay);
        btnRestaurant = findViewById(R.id.btnRestaurant);
        btnCafe = findViewById(R.id.btnCafe);

        recyclerViewSearchList = findViewById(R.id.recyclerViewSearchList);
        recyclerViewSearchList.setLayoutManager(new LinearLayoutManager(this));
        spotListAdapter = new SpotListAdapter(spotList);
        recyclerViewSearchList.setAdapter(spotListAdapter);


        // 인텐트 데이터 받기
        keyword = getIntent().getStringExtra("Keyword");
        locationType = getIntent().getStringExtra("locationType");

        // 인텐트 데이터에 따른 처리
        if (keyword != null) {
            locationType = null;
            setButtonBackgroundColor(btnAll);
            performActionBasedOnKeyword(keyword, locationType);
        } else if (locationType != null) {
            keyword = null;
            switch (locationType) {
                case "교통시설":
                    setButtonBackgroundColor(btnTransport);
                    performActionBasedOnKeyword(null, locationType);
                    break;
                case "문화 시설":
                    setButtonBackgroundColor(btnPlayground);
                    performActionBasedOnKeyword(null, locationType);
                    break;
                case "상점":
                    setButtonBackgroundColor(btnShop);
                    performActionBasedOnKeyword(null, locationType);
                    break;
                case "숙박":
                    setButtonBackgroundColor(btnStay);
                    performActionBasedOnKeyword(null, locationType);
                    break;
                case "식당":
                    setButtonBackgroundColor(btnRestaurant);
                    performActionBasedOnKeyword(null, locationType);
                    break;
                case "카페":
                    setButtonBackgroundColor(btnCafe);
                    performActionBasedOnKeyword(null, locationType);
                    break;
            }
        }


        btnAll.setOnClickListener(v -> {
            locationType = null;
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
            searchSpot(keyword, locationType);
        });

        fab.setOnClickListener(v -> {
            if (!spotList.isEmpty()) {
                Intent intent = new Intent(LocationListActivity.this, MapActivity.class);
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
                        performActionBasedOnKeyword( keyword, locationType);
                    }
                }
            }
        });


    }
    private void performActionBasedOnKeyword(String keyword, String locationType) {
        if (keyword != null && !keyword.isEmpty()) {
            searchSpot(keyword, locationType);
        } else {
            loadSpots(locationType);
        }
    }

    private void loadSpots(String locationType) {
        isLoading = true;
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        FilterApi filterApi = retrofit.create(FilterApi.class);
        Call<FilterRes> call = filterApi.filter(currentPage, pageSize, "", "", "", locationType,"", "", "","");

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

    private void searchSpot(String keyword, String locationType) {
        isLoading = true;
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        FilterApi filterApi = retrofit.create(FilterApi.class);
        Call<FilterRes> call = filterApi.search(currentPage, pageSize, "", "", "", locationType, "", "", "",keyword);

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