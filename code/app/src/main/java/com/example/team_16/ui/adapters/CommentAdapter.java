package com.example.team_16.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;
import com.example.team_16.models.Comment;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

/**
 * Adapter for the comments list in a RecyclerView
 * TODO: This is a placeholder implementation that will be fully developed when the comments feature is implemented
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
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
        private ShapeableImageView userImage;
        private TextView userName;
        private TextView commentText;
        private TextView commentTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.comment_user_image);
            userName = itemView.findViewById(R.id.comment_user_name);
            commentText = itemView.findViewById(R.id.comment_text);
            commentTime = itemView.findViewById(R.id.comment_time);
        }

        public void bind(Comment comment) {
            // TODO: When implementing comments feature, update this method to:
            // 1. Display the comment user's name
            // 2. Display the actual comment text
            // 3. Format and display the timestamp (like "2 hours ago")
            // 4. Load the user's profile image using Glide or similar library
            // 5. Implement interaction features (like, reply, etc.)

            // Placeholder implementation
            userName.setText(comment.getUserName());
            commentText.setText(comment.getText());
            commentTime.setText("Coming soon");
            userImage.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }

    // TODO: Implement full comment functionality when the feature is ready
}
