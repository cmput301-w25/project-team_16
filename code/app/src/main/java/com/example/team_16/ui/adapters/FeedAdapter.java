package com.example.team_16.ui.adapters;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.MoodEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Adapter responsible for displaying and updating recyclerView of mood events
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private Context context;
    private List<MoodEvent> moodEvents;
    private String fullName;
    private String username;
    private OnItemClickListener listener;

    /**
     * Interface for item clicks
     */
    public interface OnItemClickListener {
        void onItemClick(MoodEvent event);
    }

    /**
     * Listener for mood event item clicks
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * FeedAdapter constuctor
     */
    public FeedAdapter(Context context, List<MoodEvent> moodEvents) {
        this.context = context;
        // Sort the mood events by timestamp, with most recent on top
        if (moodEvents != null) {
            // Sort the list in descending order (newest first)
            moodEvents.sort((event1, event2) ->
                    // Compare timestamps in reverse order for descending sort
                    event2.getTimestamp().compareTo(event1.getTimestamp())
            );
        }
        this.moodEvents = moodEvents;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your item layout (ensure item_feed.xml exists with the IDs below)
        View view = LayoutInflater.from(context).inflate(R.layout.feed_recyclerview_item, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);
        EmotionalState emotionalState = event.getEmotionalState();

        // Set text values
        holder.mood_one_view.setText(emotionalState.getName());
        holder.emoji_one_view.setText(emotionalState.getEmoji());
        String date = event.getFormattedDate();
        Date actualDate = event.getTimestamp().toDate();
        holder.with_amount_view.setText(event.getSocialSituation());
        holder.mood_description_view.setText(event.getTrigger());
        holder.time_view.setText(date);

        // Get color from emotional state
        int moodColor = emotionalState.getColor();

        // Apply nearly black color to the mood text
        holder.mood_one_view.setTextColor(Color.parseColor("#111111"));

        // Apply darker color to the mood description (trigger text)
        holder.mood_description_view.setTextColor(Color.parseColor("#333333"));

        // Add spacing between emoji and emotional state text
        holder.emoji_one_view.setPadding(0, 0, 16, 0); // Add right padding to create space
        // Apply color to the with amount (making it amber/gold like in screenshot)
        holder.with_amount_view.setTextColor(Color.parseColor("#A9772B"));  // Gold/amber color from layout

        // Create a lighter version of the mood color for the background
        int red = (moodColor >> 16) & 0xFF;
        int green = (moodColor >> 8) & 0xFF;
        int blue = moodColor & 0xFF;

        // Make a lighter version (70% of the original color + 30% white) for top gradient
        int lightRed = Math.min(255, red + (255 - red) * 7 / 10);
        int lightGreen = Math.min(255, green + (255 - green) * 7 / 10);
        int lightBlue = Math.min(255, blue + (255 - blue) * 7 / 10);
        int lightColor = (0xFF << 24) | (lightRed << 16) | (lightGreen << 8) | lightBlue;

        // Make an even lighter version (85% white + 15% of the original color) for the main background
        int veryLightRed = Math.min(255, red + (255 - red) * 17 / 20);
        int veryLightGreen = Math.min(255, green + (255 - green) * 17 / 20);
        int veryLightBlue = Math.min(255, blue + (255 - blue) * 17 / 20);
        int veryLightColor = (0xFF << 24) | (veryLightRed << 16) | (veryLightGreen << 8) | veryLightBlue;

        // Try to update the top gradient
        if (holder.gradient_top_view != null) {
            holder.gradient_top_view.setColorFilter(lightColor, android.graphics.PorterDuff.Mode.SRC_ATOP);
        }

        // Try to update the main background of the entire card
        if (holder.mainConstraintLayout != null) {
            // Apply a custom drawable with the very light color
            GradientDrawable backgroundDrawable = new GradientDrawable();
            backgroundDrawable.setColor(veryLightColor);
            backgroundDrawable.setCornerRadius(16); // Match your rounded corners
            holder.mainConstraintLayout.setBackground(backgroundDrawable);
        }

        // Make the parent view and all its ancestors transparent to ensure proper transparency
        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        ViewGroup parent = (ViewGroup) holder.itemView.getParent();
        if (parent != null) {
            parent.setBackgroundColor(Color.TRANSPARENT);
        }

        // Ensure the card has proper elevation for shadow effect but transparent background
        if (holder.mainConstraintLayout != null) {
            holder.mainConstraintLayout.setElevation(8); // Add elevation for shadow
        }

        // Set loading placeholders
        holder.first_name_last_name_view.setText("Loading...");
        holder.profile_username_view.setText("");

        // Fetch user data
        FirebaseDB.getInstance(context).fetchUserById(event.getUserID(), new FirebaseDB.FirebaseCallback<Map<String, Object>>() {
            @Override
            public void onCallback(Map<String, Object> userData) {
                if (userData != null) {
                    String fullName = (String) userData.get("fullName");
                    String username = "@" + (String) userData.get("username");
                    holder.first_name_last_name_view.setText(fullName != null ? fullName : "Unknown");
                    holder.profile_username_view.setText(username != null ? username : "@unknown");
                } else {
                    holder.first_name_last_name_view.setText("Unknown User");
                    holder.profile_username_view.setText("@unknown");
                }
            }
        });

        // Calculate and display time ago
        String time_ago;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime eventDateTime = LocalDateTime.ofInstant(actualDate.toInstant(), ZoneId.systemDefault());
            Duration duration = Duration.between(eventDateTime, currentDateTime);
            int hour_difference = (int) Math.abs(duration.toHours());
            if (hour_difference >= 24) {
                int day_difference = Math.floorDiv(hour_difference, 24);
                time_ago = day_difference + " days ago";
            } else {
                time_ago = hour_difference + " hours ago";
            }
            holder.time_ago_view.setText("- " + time_ago);
        } else {
            holder.time_ago_view.setVisibility(View.GONE);
        }

        // Item click handling
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(event);
                }
            }
        });
    }

    /**
     * Return index of current item in recyclerView
     */
    @Override
    public int getItemCount() {
        return moodEvents == null ? 0 : moodEvents.size();
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView mood_one_view;
        TextView emoji_one_view;
        TextView time_ago_view;
        ImageView profile_picture_view;
        TextView first_name_last_name_view;
        TextView profile_username_view;
        TextView with_amount_view;
        TextView mood_description_view;
        ImageView mood_image_view;
        TextView time_view;
        ImageView gradient_top_view;
        ConstraintLayout topConstraintLayout; // Parent layout for the top section
        ConstraintLayout mainConstraintLayout; // Parent layout for the entire card

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            mood_one_view = itemView.findViewById(R.id.mood_one);
            emoji_one_view = itemView.findViewById(R.id.emoji_one);
            time_ago_view = itemView.findViewById(R.id.time_ago);
            profile_picture_view = itemView.findViewById(R.id.profile_picture);
            first_name_last_name_view = itemView.findViewById(R.id.first_name_last_name);
            profile_username_view = itemView.findViewById(R.id.profile_username);
            with_amount_view = itemView.findViewById(R.id.with_amount);
            mood_description_view = itemView.findViewById(R.id.mood_description);
            mood_image_view = itemView.findViewById(R.id.mood_image);
            time_view = itemView.findViewById(R.id.post_time);

            gradient_top_view = itemView.findViewById(R.id.gradient_top);
            topConstraintLayout = itemView.findViewById(R.id.constraintLayout);

            // For proper transparency in the card
            View cardRoot = (View) itemView.getParent();
            if (cardRoot != null) {
                cardRoot.setBackgroundColor(Color.TRANSPARENT);
            }

            // Get the main constraint layout using a different approach since parent.parent may fail
            ViewGroup parent = (ViewGroup) topConstraintLayout.getParent();
            if (parent != null) {
                mainConstraintLayout = parent instanceof ConstraintLayout ?
                        (ConstraintLayout) parent : null;

                // If that didn't work, try looking by ID
                if (mainConstraintLayout == null) {
                    mainConstraintLayout = itemView.findViewById(R.id.mood_frame);
                }
            }
        }
    }
}