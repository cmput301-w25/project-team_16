package com.example.team_16.ui.adapters;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;
import com.example.team_16.models.Comment;
import com.google.android.material.imageview.ShapeableImageView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView userImage;
        private final TextView userName;
        private final TextView commentText;
        private final TextView commentTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.comment_user_image);
            userName = itemView.findViewById(R.id.comment_user_name);
            commentText = itemView.findViewById(R.id.comment_text);
            commentTime = itemView.findViewById(R.id.comment_time);
        }

        public void bind(Comment comment) {
            // Basic placeholder logic
            userImage.setImageResource(android.R.drawable.sym_def_app_icon);

            userName.setText(comment.getUserName());
            commentText.setText(comment.getText());

            // Compute "time ago" from comment.getTimestamp() (stored as a long)
            long timestampMillis = comment.getTimestamp();
            String timeAgoStr = "Just now";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime commentTimeObj = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(timestampMillis),
                        ZoneId.systemDefault()
                );
                Duration diff = Duration.between(commentTimeObj, now);
                long hours = diff.toHours();

                if (hours >= 24) {
                    long days = hours / 24;
                    timeAgoStr = days == 1 ? "1 day ago" : days + " days ago";
                } else if (hours == 0) {
                    long minutes = diff.toMinutes();
                    if (minutes <= 1) {
                        timeAgoStr = "Just now";
                    } else {
                        timeAgoStr = minutes + " mins ago";
                    }
                } else {
                    timeAgoStr = hours == 1 ? "1 hour ago" : hours + " hours ago";
                }
            }

            commentTime.setText(timeAgoStr);
        }
    }
}
