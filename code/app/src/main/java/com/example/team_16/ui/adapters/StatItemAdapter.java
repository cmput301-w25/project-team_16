package com.example.team_16.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter for displaying stat items in a RecyclerView.
 * Used for both triggers and social situations in the monthly recap.
 */
public class StatItemAdapter extends RecyclerView.Adapter<StatItemAdapter.ViewHolder> {
    private final List<Map.Entry<String, Long>> items;
    private final String emptyMessage;

    /**
     * Creates a new StatItemAdapter.
     *
     * @param items The list of items to display
     * @param emptyMessage The message to display when the list is empty
     */
    public StatItemAdapter(List<Map.Entry<String, Long>> items, String emptyMessage) {
        this.items = items;
        this.emptyMessage = emptyMessage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (items.isEmpty()) {
            holder.tvTitle.setText(emptyMessage);
            holder.tvCount.setVisibility(View.GONE);
            return;
        }

        Map.Entry<String, Long> item = items.get(position);
        holder.tvTitle.setText(item.getKey());
        holder.tvCount.setText(String.format(Locale.getDefault(),
                "Occurred %d time%s",
                item.getValue(),
                item.getValue() == 1 ? "" : "s"));
        holder.tvCount.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.isEmpty() ? 1 : items.size(); // Show at least one item for empty state
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvCount;

        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvStatTitle);
            tvCount = view.findViewById(R.id.tvStatCount);
        }
    }
}