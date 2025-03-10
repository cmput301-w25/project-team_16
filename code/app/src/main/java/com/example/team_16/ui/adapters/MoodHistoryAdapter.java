package com.example.team_16.ui.adapters;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.os.Build;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.ViewHolder> {
    private Context context;
    private List<MoodEvent> moodEvents;

    public MoodHistoryAdapter(Context context, List<MoodEvent> moodEvents) {
        this.context = context;
        this.moodEvents = moodEvents != null ? moodEvents : new ArrayList<>(); // Ensure it's never null
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(context).inflate(R.layout.mood_history_recyclerview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);
        holder.mood_one_view.setText(event.getEmotionalState().getName());
        String date = event.getFormattedDate();

        Date actualDate = Date.from(event.getTimestamp().toInstant()); // Fixed conversion
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

        // Handle time_ago calculation
        String time_ago = "";
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
            holder.time_ago_view.setText(time_ago);
        } else {
            holder.time_ago_view.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return moodEvents.size(); // Return the size of moodEvents list
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mood_one_view, emoji_one_view, time_ago_view, first_name_last_name_view,
                profile_username_view, with_amount_view, mood_description_view, time_view;
        ImageView profile_picture_view, mood_image_view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mood_one_view = itemView.findViewById(R.id.moodView);
            emoji_one_view = itemView.findViewById(R.id.emojiView);
            time_ago_view = itemView.findViewById(R.id.timeView);
            profile_picture_view = itemView.findViewById(R.id.profilePicture);
            first_name_last_name_view = itemView.findViewById(R.id.fullNameView);
            profile_username_view = itemView.findViewById(R.id.profileUsername);
            with_amount_view = itemView.findViewById(R.id.withAmountView);
            mood_description_view = itemView.findViewById(R.id.moodDescription);
            mood_image_view = itemView.findViewById(R.id.moodImage);
            time_view = itemView.findViewById(R.id.postTime);
        }

    }

}
