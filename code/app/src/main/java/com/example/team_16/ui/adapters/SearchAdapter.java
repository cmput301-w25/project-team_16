/**
 * SearchAdapter is responsible for displaying a list of user profiles in the search results.
 * It supports actions such as following, unfollowing, and viewing user profiles.
 * The adapter dynamically updates UI elements based on the current user's following and pending request lists.
 */

package com.example.team_16.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
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
        String profileImageUrl = (String) user.get("profileImageUrl");

        holder.personName.setText(username);

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(profileImageUrl)
                    .placeholder(R.drawable.image)
                    .circleCrop()
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (followClickListener != null) {
                followClickListener.onUserClick(userId);
            }
        });

        if (followingIds.contains(userId)) {
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
            holder.followButton.setText("Pending");
            holder.followButton.setEnabled(false);
            holder.followButton.setBackgroundResource(R.drawable.pending_button_bg);
            holder.followButton.setTextColor(Color.parseColor("#1E293F"));
        } else {
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
        ImageView profileImage;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            personName = itemView.findViewById(R.id.personName);
            followButton = itemView.findViewById(R.id.followButton);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }
}
