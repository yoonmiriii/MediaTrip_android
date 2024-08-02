package com.hani.mediaspot.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hani.mediaspot.R;
import com.hani.mediaspot.model.Spot;

import java.util.List;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

    private List<Spot> spotList;

    public DetailAdapter(List<Spot> spotList) {
        this.spotList = spotList;
    }

    public void setSpots(List<Spot> spotList) {
        this.spotList = spotList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Spot spot = spotList.get(position);
        holder.txtTitle.setText("("+spot.getMediaType()+")"+ spot.getTitle());
        holder.txtDes.setText(spot.getLocationDes());
    }

    @Override
    public int getItemCount() {
        return spotList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtDes;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDes = itemView.findViewById(R.id.txtDes);
        }
    }
}