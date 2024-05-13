package com.example.patrol.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patrol.BroadcastItem;
import com.example.patrol.R;

import java.util.List;

public class TextAdapter extends RecyclerView.Adapter<TextAdapter.TextViewHolder> {

    private List<BroadcastItem> items;

    public TextAdapter(List<BroadcastItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text, parent, false);
        return new TextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextViewHolder holder, int position) {
        BroadcastItem item = items.get(position);
        holder.dateTextView.setText(item.getTimestamp());
        holder.textView.setText(item.isExpanded() ? item.getMessage() : item.getTitle());

        holder.textView.setOnClickListener(v -> {
            boolean expanded = item.toggleExpanded();
            holder.textView.setText(expanded ? item.getMessage() : item.getTitle());
        });
    }

    @Override
    public int getItemCount() {
        if (items != null) {
            return items.size();
        } else {
            return 0;
        }
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, textView;

        TextViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}
