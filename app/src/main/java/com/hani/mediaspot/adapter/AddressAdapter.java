package com.hani.mediaspot.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.hani.mediaspot.R;
import com.hani.mediaspot.SpotListActivity;
import com.hani.mediaspot.model.Spot;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {


    private List<Spot> spots;
    private List<String> regions;
    private Context context;
    private String city;

    public AddressAdapter(List<String> regions, String city, List<Spot> spots) {
        this.regions = regions;
        this.city = city;
        this.spots = spots;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setRegions(List<String> regions) {
        this.regions = regions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.address_subtitle_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String region = regions.get(position);
        holder.txtSubtitle.setText(region);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SpotListActivity.class);
                Log.d("AddressAdapter", "City sent: " + city);
                intent.putExtra("city", city);
                intent.putExtra("region", region);
                // Convert spot list to JSON and pass as extra
                Gson gson = new Gson();
                String spotsJson = gson.toJson(spots);
                intent.putExtra("spots", spotsJson);

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return regions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubtitle;

        ViewHolder(View itemView) {
            super(itemView);
            txtSubtitle = itemView.findViewById(R.id.txtSubtitle);
        }
    }
}