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

import retrofit2.Retrofit;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private final Context context;
    private final List<Spot> spotList;
    private final Retrofit retrofit;

    public HomeAdapter(Context context, List<Spot> spotList, Retrofit retrofit) {
        this.context = context;
        this.spotList = spotList;
        this.retrofit = retrofit;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_row2, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Spot spot = spotList.get(position);
        holder.txtLocation.setText(spot.getLocation());
        holder.txtLocationType.setText(spot.getLocationType());
        holder.txtAddress.setText(spot.getAddress());
        Glide.with(holder.itemView.getContext())
                .load(spot.getImgUrl())
                .into(holder.imgView);

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtLocation;
        TextView txtLocationType;
        TextView txtAddress;
        ImageView imgView;
        CardView cardView;
        Context context;
        public ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtLocationType = itemView.findViewById(R.id.txtLocationType);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            imgView=itemView.findViewById(R.id.imgView);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
