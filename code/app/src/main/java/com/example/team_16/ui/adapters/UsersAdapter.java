package com.example.team_16.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;
import com.example.team_16.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    public interface OnFollowClickListener {
        void onFollowClicked(User user);
    }

    private List<User> users;
    private User currentUser;
    private OnFollowClickListener followClickListener;

    public UsersAdapter(List<User> users, User currentUser, OnFollowClickListener listener) {
        this.users = users;
        this.currentUser = currentUser;
        this.followClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameTextView.setText(user.getUsername());

        // Update button based on follow state
        if (currentUser.getPendingFollow().contains(user.getUsername())) {
            holder.followButton.setText("Pending");
            holder.followButton.setEnabled(false);
        } else if (currentUser.getUserFollowing().contains(user.getUsername())) {
            holder.followButton.setText("Following");
            holder.followButton.setEnabled(false);
        } else {
            holder.followButton.setText("Follow");
            holder.followButton.setEnabled(true);
            holder.followButton.setOnClickListener(v -> {
                if (followClickListener != null) {
                    followClickListener.onFollowClicked(user);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        Button followButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.personName);
            followButton = itemView.findViewById(R.id.followButton);
        }
    }
}
