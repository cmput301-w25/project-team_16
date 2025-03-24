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
/**
 * Adapter responsible for displaying and updating recyclerView of mood events
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<MoodEvent> moodEvents;
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
        this.moodEvents = moodEvents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.mood_history_recyclerview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);

        // Basic data
        holder.moodView.setText(event.getEmotionalState().getName());
        holder.withAmountView.setText(event.getSocialSituation() == null
                ? "" : event.getSocialSituation());
        holder.moodDescription.setText(event.getTrigger());
        holder.fullNameView.setText("Loading...");
        holder.profileUsername.setText("");

        Date actualDate = event.getTimestamp().toDate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime eventDateTime = LocalDateTime.ofInstant(
                    actualDate.toInstant(), ZoneId.systemDefault()
            );
            Duration duration = Duration.between(eventDateTime, now);
            int hours = (int) duration.toHours();

            if (hours >= 24) {
                int days = hours / 24;
                holder.timeView.setText(days + " days ago");
            } else {
                holder.timeView.setText(hours + " hours ago");
            }
        } else {
            holder.timeView.setText("Time not supported");
        }

        FirebaseDB.getInstance(context).fetchUserById(event.getUserID(), userData -> {
            if (userData != null) {
                String fullName = (String) userData.get("fullName");
                String username = (String) userData.get("username");
                holder.fullNameView.setText(fullName != null ? fullName : "Unknown");
                holder.profileUsername.setText(
                        username != null ? "@" + username : "@unknown"
                );
            } else {
                holder.fullNameView.setText("Unknown User");
                holder.profileUsername.setText("@unknown");
            }
        });

        Log.d("MoodHistoryAdapter", "Binding item at position " + position);
    }

    @Override
    public int getItemCount() {
        return moodEvents.size();
    }

    public void updateData(List<MoodEvent> newData) {
        moodEvents.clear();
        moodEvents.addAll(newData);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView moodView, timeView, fullNameView, profileUsername, withAmountView, moodDescription;
        ImageView profilePicture, moodImage;
        TextView emojiView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            moodView        = itemView.findViewById(R.id.moodView);
            emojiView       = itemView.findViewById(R.id.emojiView);
            timeView        = itemView.findViewById(R.id.timeView);
            profilePicture  = itemView.findViewById(R.id.profilePicture);
            fullNameView    = itemView.findViewById(R.id.fullNameView);
            profileUsername = itemView.findViewById(R.id.profileUsername);
            withAmountView  = itemView.findViewById(R.id.withAmountView);
            moodDescription = itemView.findViewById(R.id.moodDescription);
            moodImage       = itemView.findViewById(R.id.moodImage);
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
}