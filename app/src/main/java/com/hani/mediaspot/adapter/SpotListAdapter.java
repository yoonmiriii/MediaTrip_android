package com.hani.mediaspot.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hani.mediaspot.DetailViewActivity;
import com.hani.mediaspot.R;
import com.hani.mediaspot.model.Spot;

import java.util.List;

public class SpotListAdapter extends RecyclerView.Adapter<SpotListAdapter.ViewHolder> {

    private List<Spot> spotList;
    private String city;
    private String region;


    public SpotListAdapter(List<Spot> spotList, String city, String region) {
        this.spotList = spotList;
        this.city = city;
        this.region = region;
    }

    public SpotListAdapter(List<Spot> spotList) {
        this.spotList = spotList;
    }

    public void setSpots(List<Spot> spotList) {
        this.spotList = spotList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.search_list_row, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Spot spot = spotList.get(position);
        holder.txtLocation.setText(spot.getLocation());
        holder.txtLocationType.setText(spot.getLocationType());
        holder.txtAddress.setText(spot.getAddress());

        // 이미지 로드 (Glide 라이브러리 사용)
        if (spot.getImgUrl() != null && !spot.getImgUrl().isEmpty()) {
            Glide.with(holder.imgView.getContext())
                    .load(spot.getImgUrl())
                    .placeholder(R.drawable.baseline_image_24) // 로딩 중에 표시할 기본 이미지
                    .into(holder.imgView);
        } else {
            holder.imgView.setImageResource(R.drawable.baseline_image_24); // 기본 이미지
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.context, DetailViewActivity.class);
                intent.putExtra("spot", spot);

                holder.context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return spotList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtLocation;
        TextView txtLocationType;
        TextView txtAddress;
        ImageView imgView;
        CardView cardView;
        Context context;

        ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtLocationType = itemView.findViewById(R.id.txtLocationType);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            imgView = itemView.findViewById(R.id.imgView);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
