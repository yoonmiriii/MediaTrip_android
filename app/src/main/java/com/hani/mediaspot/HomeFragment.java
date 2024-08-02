package com.hani.mediaspot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hani.mediaspot.adapter.HomeAdapter;
import com.hani.mediaspot.api.FilterApi;
import com.hani.mediaspot.api.NetworkClient;
import com.hani.mediaspot.model.FilterRes;
import com.hani.mediaspot.model.Spot;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    boolean isLoading = false;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location lastKnownLocation;

    private LinearLayout Transport, Culture, Shops, Stay, Cafe, Dining;
    private EditText editKeyword;
    private ImageButton btnSearch;
    private ArrayList<Spot> spotArrayList = new ArrayList<>();
    private ArrayList<Spot> distanceSpotArrayList = new ArrayList<>();
    private RecyclerView recyclerView1, recyclerView2;
    private HomeAdapter homeAdapter;
    private HomeAdapter distanceHomeAdapter;
    double latitude;
    double longitude;
    TextView textView1, textView2;

    private int currentPage;
    private static final int pageSize = 10;



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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        editKeyword = rootView.findViewById(R.id.editKeyword);
        btnSearch = rootView.findViewById(R.id.btnSearch);

        Transport = rootView.findViewById(R.id.Transport);
        Culture = rootView.findViewById(R.id.Culture);
        Shops = rootView.findViewById(R.id.Shops);
        Stay = rootView.findViewById(R.id.Stay);
        Cafe = rootView.findViewById(R.id.Cafe);
        Dining = rootView.findViewById(R.id.Dining);
        recyclerView1 = rootView.findViewById(R.id.recyclerView1);
        recyclerView2 = rootView.findViewById(R.id.recyclerView2);
        textView1=rootView.findViewById(R.id.textView1);
        textView2=rootView.findViewById(R.id.textView2);

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView1.setLayoutManager(layoutManager1);
        homeAdapter = new HomeAdapter(getContext(), spotArrayList, NetworkClient.getRetrofitClient(getContext()));
        recyclerView1.setAdapter(homeAdapter);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView2.setLayoutManager(layoutManager2);
        distanceHomeAdapter = new HomeAdapter(getContext(), distanceSpotArrayList, NetworkClient.getRetrofitClient(getContext()));
        recyclerView2.setAdapter(distanceHomeAdapter);

        Transport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LocationListActivity.class);
                intent.putExtra("locationType", "교통시설");
                startActivity(intent);
            }
        });

        Culture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LocationListActivity.class);
                intent.putExtra("locationType", "문화 시설");
                startActivity(intent);
            }
        });

        Shops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LocationListActivity.class);
                intent.putExtra("locationType", "상점");
                startActivity(intent);
            }
        });

        Stay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LocationListActivity.class);
                intent.putExtra("locationType", "숙박");
                startActivity(intent);
            }
        });

        Cafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LocationListActivity.class);
                intent.putExtra("locationType", "카페");
                startActivity(intent);
            }
        });

        Dining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LocationListActivity.class);
                intent.putExtra("locationType", "식당");
                startActivity(intent);
            }
        });


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Keyword = editKeyword.getText().toString().trim();
                Intent intent = new Intent(getContext(),LocationListActivity.class);
                intent.putExtra("Keyword", Keyword);
                getContext().startActivity(intent);
            }
        });
        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HotlocationActivity.class);
                startActivity(intent);
            }
        });
        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DistancelocationActivity.class);
                intent.putExtra("latitude", lastKnownLocation.getLatitude());
                intent.putExtra("longitude", lastKnownLocation.getLongitude());
                Log.i("MAIN LATLNG", "lat: "+ lastKnownLocation.getLatitude()+ "lng: "+ lastKnownLocation.getLongitude());
                startActivity(intent);
            }
        });

        recyclerView1.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        getHotLocation();
                    }
                }
            }
        });
        recyclerView2.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        getdistanceLocation(longitude, latitude);
                    }
                }
            }
        });

        getHotLocation();
        startLocationUpdates();
        requestLocationAndCallDistanceLocation();

        return rootView;
    }

    private void getHotLocation() {
        isLoading = true;
        FilterApi filterApi = NetworkClient.getRetrofitClient(getContext()).create(FilterApi.class);
        Call<FilterRes> call = filterApi.getHotLocation(currentPage, pageSize,"","");

        call.enqueue(new Callback<FilterRes>() {
            @Override
            public void onResponse(Call<FilterRes> call, Response<FilterRes> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    if (currentPage == 0) {
                        spotArrayList.clear();
                    }
                    FilterRes filterRes = response.body();
                    if (filterRes.getResult().equals("success")) {
                        spotArrayList.addAll(response.body().getItems());
                        homeAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<FilterRes> call, Throwable t) {
                isLoading = false;
                Toast.makeText(getContext(), "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getdistanceLocation(double latitude, double longitude) {
        isLoading = true;
        Log.i("HOME", "latitude : "+ latitude + "Longitude: "+ longitude);
        FilterApi filterApi = NetworkClient.getRetrofitClient(getContext()).create(FilterApi.class);
        Call<FilterRes> call = filterApi.getdistanceLocation(currentPage, pageSize, latitude, longitude,"","");

        call.enqueue(new Callback<FilterRes>() {
            @Override
            public void onResponse(Call<FilterRes> call, Response<FilterRes> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    if (currentPage == 0) {
                        distanceSpotArrayList.clear();
                    }
                    FilterRes filterRes = response.body();
                    if (filterRes.getResult().equals("success")) {
                        distanceSpotArrayList.addAll(response.body().getItems());
                        distanceHomeAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FilterRes> call, Throwable t) {
                isLoading = false;
                Toast.makeText(getContext(), "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void requestLocationAndCallDistanceLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), location -> {
                        if (location != null) {
                            lastKnownLocation = location;
                            // 전역 변수를 업데이트
                            latitude = lastKnownLocation.getLatitude();
                            longitude = lastKnownLocation.getLongitude();
                            Log.i("HomeFragment", "Updated Latitude: " + latitude + ", Longitude: " + longitude);
                            // 필요한 경우 getdistanceLocation 호출
                            getdistanceLocation(latitude, longitude);
                        } else {
                            Toast.makeText(getContext(), "Unable to get current location.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(getContext(), "Location permission is required.", Toast.LENGTH_SHORT).show();
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