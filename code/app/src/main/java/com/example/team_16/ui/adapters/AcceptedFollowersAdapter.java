package com.example.team_16.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.team_16.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to display accepted followers (people who follow me),
 * with a single "Remove" button to make them unfollow me.
 */
public class AcceptedFollowersAdapter
        extends RecyclerView.Adapter<AcceptedFollowersAdapter.ViewHolder> {

    /**
     * Model for each follower in the "accepted" list
     */
    public static class AcceptedFollower {
        public String userId;
        public String username;

        public AcceptedFollower(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }

    public interface OnRemoveListener {
        void onRemoveClicked(AcceptedFollower follower, int position);
    }

    private final List<AcceptedFollower> dataList = new ArrayList<>();
    private final OnRemoveListener removeListener;

    public AcceptedFollowersAdapter(OnRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setData(List<AcceptedFollower> newData) {
        dataList.clear();
        dataList.addAll(newData);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public AcceptedFollowersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accepted_follower, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AcceptedFollowersAdapter.ViewHolder holder, int position) {
        AcceptedFollower item = dataList.get(position);

        holder.textUsername.setText(item.username);
        holder.textUserId.setText("@" + item.username);

        holder.buttonRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemoveClicked(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageProfile;
        TextView textUsername, textUserId, textMood;
        Button buttonRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView      = (CardView) itemView;
            imageProfile  = itemView.findViewById(R.id.image_profile);
            textUsername  = itemView.findViewById(R.id.text_username);
            textUserId    = itemView.findViewById(R.id.text_user_id);
            textMood      = itemView.findViewById(R.id.text_mood);
            buttonRemove  = itemView.findViewById(R.id.button_remove);
        }
    }
}
