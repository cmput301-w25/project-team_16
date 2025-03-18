package com.example.team_16.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team_16.R;
import java.util.ArrayList;
import java.util.List;

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.ViewHolder> {

    public static class FollowingItem {
        public String userId;
        public String username;

        public FollowingItem(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }

    public interface OnRemoveListener {
        void onRemove(String userId, int position);
    }

    private List<FollowingItem> dataList = new ArrayList<>();
    private final OnRemoveListener removeListener;

    public FollowingAdapter(OnRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setData(List<FollowingItem> newData) {
        dataList.clear();
        dataList.addAll(newData);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accepted_follower, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FollowingItem item = dataList.get(position);

        holder.textUsername.setText(item.username);
        holder.textUserId.setText("@" + item.username);

        holder.buttonRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(item.userId, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textUsername, textUserId;
        Button buttonRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            textUsername = itemView.findViewById(R.id.text_username);
            textUserId = itemView.findViewById(R.id.text_user_id);
            buttonRemove = itemView.findViewById(R.id.button_remove);
        }
    }
}