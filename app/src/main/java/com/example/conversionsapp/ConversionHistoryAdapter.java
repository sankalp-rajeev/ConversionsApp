package com.example.conversionsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConversionHistoryAdapter extends RecyclerView.Adapter<ConversionHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<String> historyItems;
    private final OnHistoryItemDeleteListener deleteListener;
    private final Runnable updateDrawer; // Runnable to update the drawer

    // Listener Interface for delete actions
    public interface OnHistoryItemDeleteListener {
        void onDelete(String historyLabel);
    }

    // Constructor
    public ConversionHistoryAdapter(Context context, List<String> historyItems, OnHistoryItemDeleteListener deleteListener, Runnable updateDrawer) {
        this.context = context;
        this.historyItems = historyItems;
        this.deleteListener = deleteListener;
        this.updateDrawer = updateDrawer; // Initialize the updateDrawer field
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String historyLabel = historyItems.get(position);
        holder.historyTextView.setText(historyLabel);

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(historyLabel); // Call the delete listener
            }
            if (updateDrawer != null) {
                updateDrawer.run(); // Update the drawer menu
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    // ViewHolder class
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView historyTextView;
        final ImageButton deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            historyTextView = itemView.findViewById(R.id.historyTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
