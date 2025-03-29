package com.example.team_16.ui.adapters;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;
import com.example.team_16.models.Comment;
import com.google.android.material.imageview.ShapeableImageView;
import com.bumptech.glide.Glide;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private final String currentUserId;

    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(String commentId);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public interface OnCommentUserClickListener {
        void onUserClick(String userId);
    }

    private OnCommentUserClickListener userClickListener;

    public void setOnCommentUserClickListener(OnCommentUserClickListener listener) {
        this.userClickListener = listener;
    }

    public CommentAdapter(List<Comment> comments, String currentUserId) {
        this.comments = comments;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view, currentUserId);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment, userClickListener, deleteClickListener);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void removeComment(String commentId) {
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(commentId)) {
                comments.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView userImage;
        private final TextView userName;
        private final TextView commentText;
        private final TextView commentTime;
        private final ImageView deleteIcon;
        private final String currentUserId;

        public CommentViewHolder(@NonNull View itemView, String currentUserId) {
            super(itemView);
            this.currentUserId = currentUserId;
            userImage = itemView.findViewById(R.id.comment_user_image);
            userName = itemView.findViewById(R.id.comment_user_name);
            commentText = itemView.findViewById(R.id.comment_text);
            commentTime = itemView.findViewById(R.id.comment_time);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }

        public void bind(Comment comment,
                         OnCommentUserClickListener userClickListener,
                         OnDeleteClickListener deleteClickListener) {

            boolean isOwner = currentUserId != null &&
                    currentUserId.equals(comment.getUserId());

            deleteIcon.setVisibility(isOwner ? View.VISIBLE : View.GONE);

            deleteIcon.setOnClickListener(v -> {
                if (isOwner && deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(comment.getId());
                }
            });

            if (comment.getProfileImageUrl() != null && !comment.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(comment.getProfileImageUrl())
                        .placeholder(R.drawable.image)
                        .into(userImage);
            } else {
                userImage.setImageResource(R.drawable.image);
            }

            userName.setText(comment.getUserName());
            commentText.setText(comment.getText());

            View.OnClickListener profileClickListener = v -> {
                if (userClickListener != null) {
                    userClickListener.onUserClick(comment.getUserId());
                }
            };
            userImage.setOnClickListener(profileClickListener);
            userName.setOnClickListener(profileClickListener);

            String timeAgoStr = "Just now";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime commentTimeObj = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(comment.getTimestamp()),
                        ZoneId.systemDefault()
                );
                Duration diff = Duration.between(commentTimeObj, now);

                long hours = diff.toHours();
                if (hours >= 24) {
                    long days = hours / 24;
                    timeAgoStr = days + " day" + (days > 1 ? "s" : "") + " ago";
                } else if (hours > 0) {
                    timeAgoStr = hours + " hour" + (hours > 1 ? "s" : "") + " ago";
                } else {
                    long minutes = diff.toMinutes();
                    timeAgoStr = minutes > 1 ? minutes + " mins ago" : "Just now";
                }
            }
            commentTime.setText(timeAgoStr);

        }
    }
}