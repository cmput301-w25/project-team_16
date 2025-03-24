package com.example.team_16.ui.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.MoodEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Adapter responsible for displaying and updating the recyclerView of mood events in mood history.
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.ViewHolder> {

    private final Context context;
    private List<MoodEvent> moodEvents;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(MoodEvent event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // Constructor
    public MoodHistoryAdapter(Context context, List<MoodEvent> moodEvents) {
        this.context = context;
        this.moodEvents = moodEvents != null ? moodEvents : new ArrayList<>();
    }

    @NonNull
    @Override
    public MoodHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mood_history_recyclerview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodHistoryAdapter.ViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);

        // Top Banner: Set emotional state text, emoji, and gradient
        holder.moodView.setText(event.getEmotionalState().getName());
        holder.emojiView.setText(event.getEmotionalState().getEmoji());
        holder.moodView.setTextColor(event.getEmotionalState().getTextColor());
        if (holder.gradientTop != null) {
            holder.gradientTop.setImageResource(event.getEmotionalState().getGradientResourceId());
        }

        // Bottom Content: Set background and display social info
        if (holder.bottomContent != null) {
            holder.bottomContent.setBackgroundResource(event.getEmotionalState().getBottomGradientResourceId());
        }
        holder.withAmountView.setText(event.getSocialSituation());
        holder.moodDescription.setText(event.getTrigger());
        holder.moodDescription.setTextColor(event.getEmotionalState().getTextColor());
        // Optionally, if you want to set a second mood description:
        holder.moodDescription2.setText("");

        // Calculate and display "time ago" in the top banner
        Date actualDate = event.getTimestamp().toDate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime eventDateTime = LocalDateTime.ofInstant(actualDate.toInstant(), ZoneId.systemDefault());
            Duration duration = Duration.between(eventDateTime, currentDateTime);
            int hourDifference = (int) Math.abs(duration.toHours());
            String timeAgo;
            if (hourDifference >= 24) {
                int dayDifference = hourDifference / 24;
                timeAgo = dayDifference + " days ago";
            } else {
                timeAgo = hourDifference + " hours ago";
            }
            holder.timeView.setText(timeAgo);
        } else {
            holder.timeView.setText("Time not supported");
        }

        // Set profile details with placeholders and fetch actual user info
        holder.fullNameView.setText(R.string.loading);
        holder.profileUsername.setText("");
        FirebaseDB.getInstance(context).fetchUserById(event.getUserID(), userData -> {
            if (userData != null) {
                String fullName = (String) userData.get("fullName");
                String username = "@" + userData.get("username");
                holder.fullNameView.setText(fullName != null ? fullName : "Unknown");
                holder.profileUsername.setText(username);
            } else {
                holder.fullNameView.setText(R.string.unknown_user);
                holder.profileUsername.setText(R.string.unknown);
            }
        });

        // Item click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moodEvents == null ? 0 : moodEvents.size();
    }

    // Updated updateData method using DiffUtil for efficient updates
    public void updateData(List<MoodEvent> newData) {
        if (newData == null) {
            newData = new ArrayList<>();
        }
        List<MoodEvent> oldData = new ArrayList<>(moodEvents);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MoodEventDiffCallback(oldData, newData));
        this.moodEvents = new ArrayList<>(newData);
        diffResult.dispatchUpdatesTo(this);
    }

    // ViewHolder is now a non-static inner class to access adapter instance fields.
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Top banner views
        TextView moodView, timeView, fullNameView, profileUsername, withAmountView, moodDescription, moodDescription2, emojiView;
        ImageView gradientTop;
        // Bottom content container and image
        ConstraintLayout bottomContent;
        ImageView profilePicture, moodImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            moodView = itemView.findViewById(R.id.moodView);
            emojiView = itemView.findViewById(R.id.emojiView);
            timeView = itemView.findViewById(R.id.timeView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            fullNameView = itemView.findViewById(R.id.fullNameView);
            profileUsername = itemView.findViewById(R.id.profileUsername);
            withAmountView = itemView.findViewById(R.id.withAmountView);
            moodDescription = itemView.findViewById(R.id.moodDescription);
            moodDescription2 = itemView.findViewById(R.id.moodDescription2);
            moodImage = itemView.findViewById(R.id.moodImage);
            gradientTop = itemView.findViewById(R.id.gradient_top);
            bottomContent = itemView.findViewById(R.id.bottom_content);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    MoodEvent event = moodEvents.get(position);
                    onItemClickListener.onItemClick(event);
                }
            }
        }
    }

    // DiffUtil callback similar to FeedAdapter's implementation
    private static class MoodEventDiffCallback extends DiffUtil.Callback {
        private final List<MoodEvent> oldList;
        private final List<MoodEvent> newList;

        public MoodEventDiffCallback(List<MoodEvent> oldList, List<MoodEvent> newList) {
            this.oldList = oldList != null ? oldList : new ArrayList<>();
            this.newList = newList != null ? newList : new ArrayList<>();
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            MoodEvent oldEvent = oldList.get(oldItemPosition);
            MoodEvent newEvent = newList.get(newItemPosition);
            return oldEvent.getId().equals(newEvent.getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            MoodEvent oldEvent = oldList.get(oldItemPosition);
            MoodEvent newEvent = newList.get(newItemPosition);
            return Objects.equals(oldEvent.getEmotionalState(), newEvent.getEmotionalState())
                    && Objects.equals(oldEvent.getTrigger(), newEvent.getTrigger())
                    && Objects.equals(oldEvent.getSocialSituation(), newEvent.getSocialSituation())
                    && Objects.equals(oldEvent.getTimestamp(), newEvent.getTimestamp());
        }
    }
}
