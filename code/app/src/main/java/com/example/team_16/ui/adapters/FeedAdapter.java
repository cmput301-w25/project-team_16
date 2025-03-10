package com.example.team_16.ui.adapters;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
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

        holder.mood_one_view.setText(event.getEmotionalState().getName());
        holder.emoji_one_view.setText(event.getEmotionalState().getEmoji());
        String date = event.getFormattedDate();
        Date actualDate = event.getTimestamp().toDate();
        holder.with_amount_view.setText(event.getSocialSituation());
        holder.mood_description_view.setText(event.getTrigger());
        holder.time_view.setText(date);

        holder.first_name_last_name_view.setText("Loading...");
        holder.profile_username_view.setText("");

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

        String time_ago;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime eventDateTime = LocalDateTime.ofInstant(actualDate.toInstant(), ZoneId.systemDefault());
            Duration duration = Duration.between(eventDateTime, currentDateTime); // Note: order was reversed
            int hour_difference = (int) Math.abs(duration.toHours());
            if (hour_difference >= 24) {
                int day_difference = Math.floorDiv(hour_difference, 24);
                time_ago = day_difference + " days ago";
            } else {
                time_ago = hour_difference + " hours ago";
            }
            holder.time_ago_view.setText(time_ago);
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
        TextView mood_one_view = itemView.findViewById(R.id.mood_one);
        TextView emoji_one_view = itemView.findViewById(R.id.emoji_one);
        //TextView emoji_two_view = itemView.findViewById(R.id.emoji_two);
        TextView time_ago_view = itemView.findViewById(R.id.time_ago);
        ImageView profile_picture_view = itemView.findViewById(R.id.profile_picture);
        TextView first_name_last_name_view = itemView.findViewById(R.id.first_name_last_name);
        TextView profile_username_view = itemView.findViewById(R.id.profile_username);
        TextView with_amount_view = itemView.findViewById(R.id.with_amount);
        TextView mood_description_view = itemView.findViewById(R.id.mood_description);
        ImageView mood_image_view = itemView.findViewById(R.id.mood_image);
        TextView time_view = itemView.findViewById(R.id.post_time);

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            mood_one_view = itemView.findViewById(R.id.mood_one);
            emoji_one_view = itemView.findViewById(R.id.emoji_one);
            //emoji_two_view = itemView.findViewById(R.id.emoji_two);
            time_ago_view = itemView.findViewById(R.id.time_ago);
            profile_picture_view = itemView.findViewById(R.id.profile_picture);
            first_name_last_name_view = itemView.findViewById(R.id.first_name_last_name);
            profile_username_view = itemView.findViewById(R.id.profile_username);
            with_amount_view = itemView.findViewById(R.id.with_amount);
            mood_description_view = itemView.findViewById(R.id.mood_description);
            mood_image_view = itemView.findViewById(R.id.mood_image);
            time_view = itemView.findViewById(R.id.post_time);
        }

    }
}
