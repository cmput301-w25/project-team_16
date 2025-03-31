/**
 * FeedAdapter binds mood events to the feed RecyclerView.
 * It handles dynamic content such as emotional state visuals,
 * user profile info, and optional mood images.
 * Implements DiffUtil for efficient list updates and supports user/profile click interactions.
 */

package com.example.team_16.ui.adapters;

import android.app.Activity;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.MoodEvent;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import androidx.recyclerview.widget.DiffUtil;
import java.util.ArrayList;
import java.util.Objects;


public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private final Context context;
    private List<MoodEvent> moodEvents;
    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(MoodEvent event);
    }

    public interface OnProfileClickListener {
        void onProfileClick(String userId);
    }

    private OnProfileClickListener profileClickListener;

    public void setOnProfileClickListener(OnProfileClickListener listener) {
        this.profileClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public FeedAdapter(Context context, List<MoodEvent> moodEvents) {
        this.context = context;
        this.moodEvents = moodEvents != null ? moodEvents : new ArrayList<>();
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_recyclerview_item, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);

        holder.mood_one_view.setText(event.getEmotionalState().getName());
        holder.emoji_one_view.setText(event.getEmotionalState().getEmoji());
        holder.mood_one_view.setTextColor(event.getEmotionalState().getTextColor());

        if (holder.gradient_top_view != null) {
            holder.gradient_top_view.setImageResource(event.getEmotionalState().getGradientResourceId());
        }

        if (holder.bottom_content_view != null) {
            holder.bottom_content_view.setBackgroundResource(event.getEmotionalState().getBottomGradientResourceId());
        }

        String date = event.getFormattedDate();
        Date actualDate = event.getTimestamp().toDate();
        holder.with_amount_view.setText(event.getSocialSituation());
        if (event.getTrigger().equals("") && event.getPhotoFilename() != null) {
            holder.mood_description_view.setVisibility(View.GONE);
            holder.mood_description_view2.setVisibility(View.GONE);
        }
        holder.mood_description_view.setText(event.getTrigger());
        holder.mood_description_view.setTextColor(event.getEmotionalState().getTextColor());
        holder.time_view.setText(date);

        holder.first_name_last_name_view.setText(R.string.loading);
        holder.profile_username_view.setText("");

        if (event.getPhotoFilename() != null) {
            holder.mood_image_view.setVisibility(View.VISIBLE);
            Log.e("log", "images/" + event.getUserID() + "_mood.jpg");
            FirebaseDB.getInstance(context).getReference(event.getPhotoFilename())
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Glide.with(((Activity) context))
                                .load(uri)
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                                .into(holder.mood_image_view);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(((Activity) context), "Failed to load image", Toast.LENGTH_SHORT).show();
                    });
        } else {
            holder.mood_image_view.setVisibility(View.GONE);
        }

        FirebaseDB.getInstance(context).fetchUserById(event.getUserID(), userData -> {
            if (userData != null) {
                String fullName = (String) userData.get("fullName");
                String username = "@" + userData.get("username");
                holder.first_name_last_name_view.setText(fullName != null ? fullName : "Unknown");
                holder.profile_username_view.setText(username);

                String profileImageUrl = (String) userData.get("profileImageUrl");
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.image)
                            .circleCrop()
                            .into(holder.profile_picture_view);
                } else {
                    holder.profile_picture_view.setImageResource(R.drawable.image);
                }
            } else {
                holder.first_name_last_name_view.setText(R.string.unknown_user);
                holder.profile_username_view.setText(R.string.unknown);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime eventDateTime = LocalDateTime.ofInstant(actualDate.toInstant(), ZoneId.systemDefault());
            Duration duration = Duration.between(eventDateTime, currentDateTime);
            int hour_difference = (int) Math.abs(duration.toHours());
            String time_ago;
            if (hour_difference >= 24) {
                int day_difference = hour_difference / 24;
                time_ago = day_difference + " days ago";
            } else {
                time_ago = hour_difference + " hours ago";
            }
            holder.time_ago_view.setText(time_ago);
        } else {
            holder.time_ago_view.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(event);
            }
        });

        View.OnClickListener profileTap = v -> {
            if (profileClickListener != null) {
                profileClickListener.onProfileClick(event.getUserID());
            }
        };

        holder.first_name_last_name_view.setOnClickListener(profileTap);
        holder.profile_username_view.setOnClickListener(profileTap);
        holder.profile_picture_view.setOnClickListener(profileTap);
    }

    @Override
    public int getItemCount() {
        return moodEvents == null ? 0 : moodEvents.size();
    }

    public void updateData(List<MoodEvent> newData) {
        if (newData == null) {
            newData = new ArrayList<>();
        }

        List<MoodEvent> oldData = new ArrayList<>();
        if (moodEvents != null) {
            oldData.addAll(moodEvents);
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new MoodEventDiffCallback(oldData, newData)
        );

        this.moodEvents = new ArrayList<>(newData);

        diffResult.dispatchUpdatesTo(this);
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
        TextView mood_description_view2;
        ImageView mood_image_view;
        TextView time_view;
        ImageView gradient_top_view;
        ConstraintLayout bottom_content_view;

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
            mood_description_view2 = itemView.findViewById(R.id.mood_description2);
            mood_image_view = itemView.findViewById(R.id.mood_image);
            time_view = itemView.findViewById(R.id.post_time);
            gradient_top_view = itemView.findViewById(R.id.gradient_top);
            bottom_content_view = itemView.findViewById(R.id.bottom_content);
        }
    }

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

            boolean sameEmotionalState = Objects.equals(
                    oldEvent.getEmotionalState(),
                    newEvent.getEmotionalState()
            );

            boolean sameTrigger = Objects.equals(
                    oldEvent.getTrigger(),
                    newEvent.getTrigger()
            );

            boolean sameSocialSituation = Objects.equals(
                    oldEvent.getSocialSituation(),
                    newEvent.getSocialSituation()
            );

            boolean sameTimestamp = Objects.equals(
                    oldEvent.getTimestamp(),
                    newEvent.getTimestamp()
            );

            return sameEmotionalState && sameTrigger &&
                    sameSocialSituation && sameTimestamp;
        }
    }
}