package com.example.team_16.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.MoodHistory;
import com.example.team_16.models.UserProfile;
import com.google.protobuf.NullValue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MoodDetails#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoodDetails extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private MoodEvent moodEvent;
    private String moodID;
    private String fullName;
    private String username;


    public MoodDetails() {
        //
    }

    public static MoodDetails newInstance(String param1) {
        MoodDetails fragment = new MoodDetails();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            moodID = getArguments().getString(ARG_PARAM1);
//        }
//
//        if (moodID.equals("") == Boolean.FALSE) {
//            FirebaseDB.getInstance(requireContext()).getMoodEventFromID(moodID, new FirebaseDB.FirebaseCallback<MoodEvent>() {
//                @Override
//                public void onCallback(MoodEvent moodEvent) {
//                    if (moodEvent != null) {
//                        MoodDetails.this.moodEvent = moodEvent;
//                        Log.d("MoodDetails", "MoodEvent retrieved: ");
//
//                        FirebaseDB.getInstance(requireContext()).fetchUserById(moodEvent.getUserID(), new FirebaseDB.FirebaseCallback<Map<String, Object>>() {
//                            @Override
//                            public void onCallback(Map<String, Object> userData) {
//                                if (userData != null) {
//
//                                    fullName = (String) userData.get("fullName");
//                                    username = "@" + (String) userData.get("username");
//                                } else {
//                                    Log.d("MoodDetails", "User data not found.");
//                                }
//                            }
//                        });
//                    }
//                    else {
//                        Log.d("MoodDetails", "MoodEvent not found.");
//                    }
//                }
//            });
//        }
//
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            moodID = getArguments().getString(ARG_PARAM1);
        }

        if (moodID.equals("") == Boolean.FALSE) {
            FirebaseDB.getInstance(requireContext()).getMoodEventFromID(moodID, new FirebaseDB.FirebaseCallback<MoodEvent>() {
                @Override
                public void onCallback(MoodEvent moodEvent) {
                    if (moodEvent != null) {
                        MoodDetails.this.moodEvent = moodEvent;
                        Log.d("MoodDetails", "MoodEvent retrieved: ");

                        FirebaseDB.getInstance(requireContext()).fetchUserById(moodEvent.getUserID(), new FirebaseDB.FirebaseCallback<Map<String, Object>>() {
                            @Override
                            public void onCallback(Map<String, Object> userData) {
                                if (userData != null) {

                                    fullName = (String) userData.get("fullName");
                                    username = "@" + (String) userData.get("username");
                                } else {
                                    Log.d("MoodDetails", "User data not found.");
                                }
                            }
                        });
                    }
                    else {
                        Log.d("MoodDetails", "MoodEvent not found.");
                    }
                }
            });
        }
        else {



        }

        View view = inflater.inflate(R.layout.fragment_mood_details, container, false);

        TextView mood_one_view = view.findViewById(R.id.mood_one);
        TextView emoji_one_view = view.findViewById(R.id.emoji_one);
        TextView emoji_two_view = view.findViewById(R.id.emoji_two);
        TextView time_ago_view = view.findViewById(R.id.time_ago);
        ImageView profile_picture_view = view.findViewById(R.id.profile_picture);
        TextView first_name_last_name_view = view.findViewById(R.id.first_name_last_name);
        TextView profile_username_view = view.findViewById(R.id.profile_username);
        TextView with_amount_view = view.findViewById(R.id.with_amount);
        TextView mood_description_view = view.findViewById(R.id.mood_description);
        ImageView mood_image_view = view.findViewById(R.id.mood_image);
        TextView time_view = view.findViewById(R.id.post_time);
        ImageView edit_button_view = view.findViewById(R.id.edit_button);
        ImageView back_button_view = view.findViewById(R.id.back_button);

        mood_one_view.setText(moodEvent.getEmotionalState().getName());
        String date = moodEvent.getFormattedDate();
        Date actualDate = moodEvent.getTimestamp().toDate();
        String time_ago = "N/A";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            Duration duration = Duration.between(currentDateTime, (Temporal) actualDate);
            int hour_difference = (int) Math.abs(duration.toHours());
            if (hour_difference >= 24) {
                int day_difference = Math.floorDiv(hour_difference, 24);
                time_ago = day_difference + " days ago";
            }
            else {
                time_ago = hour_difference + " hours ago";
            }
        }
        else {
            time_ago_view.setVisibility(View.GONE);
        }
        time_ago_view.setText(time_ago);


        first_name_last_name_view.setText(fullName);
        profile_username_view.setText(username);
        with_amount_view.setText(moodEvent.getSocialSituation());
        mood_description_view.setText(moodEvent.getTrigger());
        time_view.setText(date);



        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mood_details, container, false);
    }


    public void receiveData(MoodEvent moodEvent) {
        this.moodEvent = moodEvent;
    }



}
