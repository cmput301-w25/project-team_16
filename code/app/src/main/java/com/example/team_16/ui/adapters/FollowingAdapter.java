package com.example.team_16.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.team_16.R;
import java.util.ArrayList;
import java.util.List;

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.ViewHolder> {

    public static class FollowingItem {
        public String userId;
        public String username;
        public String profileImageUrl;

        public FollowingItem(String userId, String username, String profileImageUrl) {
            this.userId = userId;
            this.username = username;
            this.profileImageUrl = profileImageUrl;
        }
    }
    public interface OnItemClickListener {
        void onItemClick(String userId);
    }

    public interface OnRemoveListener {
        void onRemove(String userId, int position);
    }

    private List<FollowingItem> dataList = new ArrayList<>();
    private final OnRemoveListener removeListener;
    private final OnItemClickListener itemClickListener;

    public FollowingAdapter(OnRemoveListener removeListener, OnItemClickListener itemClickListener) {
        this.removeListener = removeListener;
        this.itemClickListener = itemClickListener;
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

        if (holder.imageProfile != null) {
            Glide.with(holder.itemView.getContext())
                    .load(item.profileImageUrl)
                    .placeholder(R.drawable.image)
                    .fallback(R.drawable.image)
                    .circleCrop()
                    .into(holder.imageProfile);
        } else {
            Log.e("FollowingAdapter", "imageProfile is null for position: " + position);
        }
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(item.userId);
            }
        });

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
        ImageView imageProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            textUsername = itemView.findViewById(R.id.text_username);
            imageProfile = itemView.findViewById(R.id.profile_image);
            textUserId = itemView.findViewById(R.id.text_user_id);
            buttonRemove = itemView.findViewById(R.id.button_remove);
        }
    }
}