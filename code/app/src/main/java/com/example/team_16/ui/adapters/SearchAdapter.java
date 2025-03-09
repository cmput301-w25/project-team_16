package com.example.team_16.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
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

        if (followingIds.contains(userId)) {
            holder.followButton.setText("Following");
            holder.followButton.setEnabled(false);
            holder.followButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAB8C2")));
        } else if (pendingIds.contains(userId)) {
            holder.followButton.setText("Pending");
            holder.followButton.setEnabled(false);
            holder.followButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
        } else {
            holder.followButton.setText("Follow");
            holder.followButton.setEnabled(true);
            holder.followButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }

        holder.followButton.setOnClickListener(v -> {
            if (followClickListener != null) {
                followClickListener.onFollowClick(userId);
            }
        });
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