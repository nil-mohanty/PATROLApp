package com.example.patrol.utils;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.patrol.DTO.DemandItem;
import com.example.patrol.R;
import java.util.List;

public class DemandAdapter extends RecyclerView.Adapter<DemandAdapter.ViewHolder> {
    private List<DemandItem> demandItemList;

    public DemandAdapter(List<DemandItem> demandItemList) {
        this.demandItemList = demandItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_demand, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DemandItem currentItem = demandItemList.get(position); // Adjust position to account for header
            holder.productNameTextView.setText(currentItem.getProductName());
            holder.demandAmountTextView.setText(String.valueOf(currentItem.getDemandAmount()));

    }

    @Override
    public int getItemCount() {
        return demandItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView;
        TextView demandAmountTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            demandAmountTextView = itemView.findViewById(R.id.demandAmountTextView);
        }
    }
}


