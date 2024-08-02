package com.hani.mediaspot;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.hani.mediaspot.adapter.AddressAdapter;
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



public class AddressFragment extends Fragment {


    private ImageButton btnSearch;

    private TextView[] txtViews;

    private RecyclerView recyclerViewSubtitle;
    private AddressAdapter addressAdapter;
    private List<Spot> spotList = new ArrayList<>();

    private String city = "강원도";



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);
        Log.i("LOCATION", city);

        txtViews = new TextView[] {
                view.findViewById(R.id.txtView1),
                view.findViewById(R.id.txtView2),
                view.findViewById(R.id.txtView3),
                view.findViewById(R.id.txtView4),
                view.findViewById(R.id.txtView5),
                view.findViewById(R.id.txtView6),
                view.findViewById(R.id.txtView7),
                view.findViewById(R.id.txtView8),
                view.findViewById(R.id.txtView9),
                view.findViewById(R.id.txtView10),
                view.findViewById(R.id.txtView11),
                view.findViewById(R.id.txtView12),
                view.findViewById(R.id.txtView13),
                view.findViewById(R.id.txtView14)
        };

        recyclerViewSubtitle = view.findViewById(R.id.recyclerViewSubtitle);
        recyclerViewSubtitle.setLayoutManager(new LinearLayoutManager(getContext()));
        addressAdapter = new AddressAdapter(new ArrayList<>(), city, spotList);
        recyclerViewSubtitle.setAdapter(addressAdapter);

        setTextViewClickListener(txtViews);


        selectCity(city);



        return view;
    }

    private void setTextViewClickListener(TextView[] textViews) {
        for (TextView textView : textViews) {
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetTextViewBackgrounds();
                    view.setBackgroundColor(getResources().getColor(R.color.white));
                    city = ((TextView) view).getText().toString();
                    Log.d("AddressFragment", "City selected: " + city);
                    fetchRegionsForCity(city);
                }
            });
        }
    }

    private void selectCity(String city) {
        for (TextView textView : txtViews) {
            if (textView.getText().toString().equals(city)) {
                textView.performClick();
                break;
            }
        }
    }

    private void resetTextViewBackgrounds() {
        for (TextView textView : txtViews) {
            textView.setBackgroundColor(getResources().getColor(R.color.gray));
        }
    }

    private void fetchRegionsForCity(String city) {
        this.city = city;
        Log.i("LOCATION", city);
        Retrofit retrofit = NetworkClient.getRetrofitClient(getContext());
        FilterApi filterApi = retrofit.create(FilterApi.class);
        Call<FilterRes> call = filterApi.filterLocation(0, 50, city);
        Log.i("LOCATION", city);
        call.enqueue(new Callback<FilterRes>() {
            @Override
            public void onResponse(Call<FilterRes> call, Response<FilterRes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    spotList.clear();
                    spotList.addAll(response.body().getItems());

                    // 어댑터의 텍스트뷰 업데이트
                    updateAdapterWithRegions();
                } else {
                    Toast.makeText(getContext(),
                            "데이터를 가져오는데 실패하였습니다, 다시 시도해주세요",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FilterRes> call, Throwable t) {
                Toast.makeText(getContext(),
                        "데이터 가져오기 실패: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAdapterWithRegions() {
        List<String> regions = new ArrayList<>();
        for (Spot spot : spotList) {
            if (!regions.contains(spot.getRegion())) {
                regions.add(spot.getRegion());
            }
        }
        addressAdapter.setRegions(regions);
        addressAdapter.setCity(city);
    }
}