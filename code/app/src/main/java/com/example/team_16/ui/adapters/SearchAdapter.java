package com.example.team_16.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team_16.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    private List<Map<String, Object>> users = new ArrayList<>();
    private List<String> followingIds = new ArrayList<>();
    private List<String> pendingIds = new ArrayList<>();
    private final OnFollowClickListener followClickListener;

    public interface OnFollowClickListener {
        void onFollowClick(String targetUserId);
        void onUnfollowClick(String targetUserId);

        // New - added to click on users
        void onUserClick(String targetUserId);

    }

    public SearchAdapter(OnFollowClickListener listener) {
        this.followClickListener = listener;
    }

    public void updateLists(List<String> following, List<String> pending) {
        this.followingIds = following;
        this.pendingIds = pending;
        notifyDataSetChanged();
    }

    public void setUsers(List<Map<String, Object>> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_recycler_view, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Map<String, Object> user = users.get(position);
        String username = (String) user.get("username");
        String userId = (String) user.get("id");

        holder.personName.setText(username);

        // New code to click on profile - Entire row clicked -> open user profile
        holder.itemView.setOnClickListener(v -> {
            if (followClickListener != null) {
                followClickListener.onUserClick(userId);
            }
        });
        // end of new code

        if (followingIds.contains(userId)) {
            // UNFOLLOW state
            holder.followButton.setText("Unfollow");
            holder.followButton.setEnabled(true);
            holder.followButton.setBackgroundResource(R.drawable.unfollow_button_bg);
            holder.followButton.setTextColor(Color.RED);
            holder.followButton.setOnClickListener(v -> {
                if (followClickListener != null) {
                    followClickListener.onUnfollowClick(userId);
                }
            });
        } else if (pendingIds.contains(userId)) {
            // PENDING state
            holder.followButton.setText("Pending");
            holder.followButton.setEnabled(false);
            holder.followButton.setBackgroundResource(R.drawable.pending_button_bg);
            holder.followButton.setTextColor(Color.parseColor("#1E293F"));
        } else {
            // FOLLOW state
            holder.followButton.setText("Follow");
            holder.followButton.setEnabled(true);
            holder.followButton.setBackgroundResource(R.drawable.follow_button_bg);
            holder.followButton.setTextColor(Color.parseColor("#4CAF50"));
            holder.followButton.setOnClickListener(v -> {
                if (followClickListener != null) {
                    followClickListener.onFollowClick(userId);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView personName;
        AppCompatButton followButton;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            personName = itemView.findViewById(R.id.personName);
            followButton = itemView.findViewById(R.id.followButton);
        }
    }
}
