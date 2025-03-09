package com.example.team_16.ui.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MoodDetails#newInstance} factory method to
 * create an instance of this fragment.
 */
//public class MoodDetails extends Fragment {
//
//    private static final String ARG_PARAM1 = "param1";
//    private MoodEvent moodEvent;
//    private String moodID;
//    private String fullName;
//    private String username;
//    private UserProfile userProfile;
//    private Date actualDate;
//    private String time_ago;
//
//
//    public MoodDetails() {
//        //
//    }
//
//    public static MoodDetails newInstance(String param1) {
//        MoodDetails fragment = new MoodDetails();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
//
//        if (getArguments() != null) {
//            moodID = getArguments().getString(ARG_PARAM1);
//        }
//
////        if (moodID.equals("") == Boolean.FALSE) {
////            FirebaseDB.getInstance(requireContext()).getMoodEventFromID(moodID, new FirebaseDB.FirebaseCallback<MoodEvent>() {
////                @Override
////                public void onCallback(MoodEvent moodEvent) {
////                    if (moodEvent != null) {
////                        MoodDetails.this.moodEvent = moodEvent;
////                        Log.d("MoodDetails", "MoodEvent retrieved: ");
////
////                        FirebaseDB.getInstance(requireContext()).fetchUserById(moodEvent.getUserID(), new FirebaseDB.FirebaseCallback<Map<String, Object>>() {
////                            @Override
////                            public void onCallback(Map<String, Object> userData) {
////                                if (userData != null) {
////
////                                    fullName = (String) userData.get("fullName");
////                                    username = "@" + (String) userData.get("username");
////                                } else {
////                                    Log.d("MoodDetails", "User data not found.");
////                                }
////                            }
////                        });
////                    }
////                    else {
////                        Log.d("MoodDetails", "MoodEvent not found.");
////                    }
////                }
////            });
////        }
//
//    }
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_mood_details, container, false);
//        return view;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//
//        TextView mood_one_view = view.findViewById(R.id.mood_one);
//        TextView emoji_one_view = view.findViewById(R.id.emoji_one);
//        TextView emoji_two_view = view.findViewById(R.id.emoji_two);
//        TextView time_ago_view = view.findViewById(R.id.time_ago);
//        ImageView profile_picture_view = view.findViewById(R.id.profile_picture);
//        TextView first_name_last_name_view = view.findViewById(R.id.first_name_last_name);
//        TextView profile_username_view = view.findViewById(R.id.profile_username);
//        TextView with_amount_view = view.findViewById(R.id.with_amount);
//        TextView mood_description_view = view.findViewById(R.id.mood_description);
//        ImageView mood_image_view = view.findViewById(R.id.mood_image);
//        TextView time_view = view.findViewById(R.id.post_time);
//        ImageView edit_button_view = view.findViewById(R.id.edit_button);
//
//
//        if (getArguments() != null) {
//            moodID = getArguments().getString(ARG_PARAM1);
//        }
//
//        if (moodID.equals("") == Boolean.FALSE) {
//            // attempt to get mood event from ID
//            FirebaseDB.getInstance(requireContext()).getMoodEventFromID(moodID, new FirebaseDB.FirebaseCallback<MoodEvent>() {
//                @Override
//                public void onCallback(MoodEvent moodEvent) {
//                    if (moodEvent != null) {
//                        MoodDetails.this.moodEvent = moodEvent;
//                        Log.d("MoodDetails", "MoodEvent retrieved.");
//
//                        FirebaseDB.getInstance(requireContext()).fetchUserById(moodEvent.getUserID(), new FirebaseDB.FirebaseCallback<Map<String, Object>>() {
//                            @Override
//                            public void onCallback(Map<String, Object> userData) {
//                                if (userData != null) {
//                                    fullName = (String) userData.get("fullName");
//                                    username = "@" + (String) userData.get("username");
//                                    mood_one_view.setText(moodEvent.getEmotionalState().getName());
//                                    String date = moodEvent.getFormattedDate();
//                                    actualDate = moodEvent.getTimestamp().toDate();
//                                    first_name_last_name_view.setText(fullName);
//                                    profile_username_view.setText(username);
//                                    with_amount_view.setText(moodEvent.getSocialSituation());
//                                    mood_description_view.setText(moodEvent.getTrigger());
//                                    time_view.setText(date);
//
//                                } else {
//                                    Toast.makeText(requireContext(), "Could not get mood event user, please try again.", Toast.LENGTH_SHORT).show();
//                                    requireActivity().finish();
//                                    return;
//                                }
//                            }
//                        });
//                    }
//                    else {
//                        Toast.makeText(requireContext(), "MoodEvent not found, please try again.", Toast.LENGTH_SHORT).show();
//                        Log.d("MoodDetails", "MoodEvent not found.");
//                        requireActivity().finish();
//                        return;
//                    }
//                }
//            });
//        }
//
//        else if (moodEvent != null) {
//
//            mood_one_view.setText(moodEvent.getEmotionalState().getName());
//            String date = moodEvent.getFormattedDate();
//            actualDate = moodEvent.getTimestamp().toDate();
//            first_name_last_name_view.setText(fullName);
//            profile_username_view.setText(username);
//            with_amount_view.setText(moodEvent.getSocialSituation());
//            mood_description_view.setText(moodEvent.getTrigger());
//            time_view.setText(date);
//
//            FirebaseDB.getInstance(requireContext()).fetchUserById(moodEvent.getUserID(), new FirebaseDB.FirebaseCallback<Map<String, Object>>() {
//                @Override
//                public void onCallback(Map<String, Object> userData) {
//                    if (userData != null) {
//                        fullName = (String) userData.get("fullName");
//                        username = "@" + (String) userData.get("username");
//                        mood_one_view.setText(moodEvent.getEmotionalState().getName());
//                        String date = moodEvent.getFormattedDate();
//                        actualDate = moodEvent.getTimestamp().toDate();
//                        first_name_last_name_view.setText(fullName);
//                        profile_username_view.setText(username);
//                        with_amount_view.setText(moodEvent.getSocialSituation());
//                        mood_description_view.setText(moodEvent.getTrigger());
//                        time_view.setText(date);
//
//                    } else {
//                        Toast.makeText(requireContext(), "Could not get mood event user, please try again.", Toast.LENGTH_SHORT).show();
//                        requireActivity().finish();
//                        return;
//                    }
//                }
//            });
//        //}
//
//            //TODO: images
//            //String mood_image_fileurl = intent.getStringExtra("Mood_Image_URL");
//            //String profile_image_fileurl = intent.getStringExtra("Profile_Image_URL");
//
//        }
//
//        else {
//            Toast.makeText(requireContext(), "No mood event to find/display, please try again.", Toast.LENGTH_SHORT).show();
//            Log.d("MoodDetails", "No mood event.");
//            requireActivity().finish();
//            return;
//        }
//
//        // if mood detail is not the users own, hide and disable edit view button
//        if (userProfile.getId().equals(moodEvent.getUserID())) {
//            edit_button_view.setVisibility(View.GONE);
//        }
//
//        // calculate time since mood event was posted
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            LocalDateTime currentDateTime = LocalDateTime.now();
//            Duration duration = Duration.between(currentDateTime, (Temporal) actualDate);
//            int hour_difference = (int) Math.abs(duration.toHours());
//            if (hour_difference >= 24) {
//                int day_difference = Math.floorDiv(hour_difference, 24);
//                time_ago = day_difference + " days ago";
//            }
//            else {
//                time_ago = hour_difference + " hours ago";
//            }
//        }
//        else {
//            time_ago_view.setVisibility(View.GONE);
//
//        }
//        time_ago_view.setText(time_ago);
//
//
//
//        edit_button_view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (userProfile == null ) {
//                    Toast.makeText(requireContext(), "Cannot load user profile, please try again.", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    //go to edit mood event activity when edit button is pressed
//                    //Intent intent2 = new Intent(MoodDetails.this, EditMood.class);
//                    //pass needed info to EditMood here
//                    //editMoodEventLauncher.launch(intent); <-- launch EditMood on button press
//                }
//            }
//        });
//
//
//    }
//
//    image code for onViewCreated
//
//
//    // optional image visibility check
//        if (mood_image_fileurl.equals("")) {
//        mood_image_view.setVisibility(View.GONE);
//    }
//
//    // For photos, we can add Glide to our dependencies, sync gradle, then use the following code:
//            Glide.with(this)
//                    .load(mood_image_fileurl) // Replace this with firebase image url, which should be saved to the database (along with the image itself) during the uploading of the image
//    // .placeholder(R.drawable.placeholder) // optional, we can add a placeholder while loading
//    // .error(R.drawable.error_image) // also optional, we can add an error image if it doesnt load
//                    .into(mood_image_view);
//
//    // doing the same for the profile picture:
//            Glide.with(this)
//                    .load(profile_image_fileurl)
//                    .into(profile_picture_view);
//
//
//    public void receiveData(MoodEvent moodEvent) {
//        this.moodEvent = moodEvent;
//    }
//
//
//
//}



public class MoodDetails extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private MoodEvent moodEvent;
    private String moodID;
    private String fullName;
    private String username;
    private UserProfile userProfile;
    private Date actualDate;
    private String time_ago;

    public MoodDetails() {
    }

    public static MoodDetails newInstance(String param1) {
        MoodDetails fragment = new MoodDetails();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
        if (getArguments() != null) {
            moodID = getArguments().getString(ARG_PARAM1);
        }

        if (userProfile == null) {
            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        // Retrieve the list of mood events from the followed users' history
        MoodHistory followingMoodHistory = userProfile.getFollowingMoodHistory();
        if (followingMoodHistory == null) {
            Log.e("log", "mood history is null");
        }
        else {
            Log.e("log", "history not null");
            Log.e("log", followingMoodHistory.toString());
            Log.e("log", followingMoodHistory.getAllEvents().toString());

        }

        List<MoodEvent> moodEvents = followingMoodHistory.getAllEvents();

        for (MoodEvent currentMoodEvent : moodEvents) {
            if (currentMoodEvent.getId().equals(moodID)) {
                moodEvent = currentMoodEvent;
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mood_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getView() == null || moodEvent == null) {
            return;
        }
        TextView mood_one_view = getView().findViewById(R.id.mood_one);
        TextView emoji_one_view = getView().findViewById(R.id.emoji_one);
        TextView time_ago_view = getView().findViewById(R.id.time_ago);
        ImageView profile_picture_view = getView().findViewById(R.id.profile_picture);
        TextView first_name_last_name_view = getView().findViewById(R.id.first_name_last_name);
        TextView profile_username_view = getView().findViewById(R.id.profile_username);
        TextView with_amount_view = getView().findViewById(R.id.with_amount);
        TextView mood_description_view = getView().findViewById(R.id.mood_description);
        ImageView mood_image_view = getView().findViewById(R.id.mood_image);
        TextView time_view = getView().findViewById(R.id.post_time);

        mood_one_view.setText(moodEvent.getEmotionalState().getName());
        emoji_one_view.setText(moodEvent.getEmotionalState().getEmoji());
        String date = moodEvent.getFormattedDate();
        actualDate = moodEvent.getTimestamp().toDate();
        first_name_last_name_view.setText(fullName);
        profile_username_view.setText(username);
        with_amount_view.setText(moodEvent.getSocialSituation());
        mood_description_view.setText(moodEvent.getTrigger());
        time_view.setText(date);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime eventDateTime = LocalDateTime.ofInstant(actualDate.toInstant(), ZoneId.systemDefault());
            Duration duration = Duration.between(currentDateTime, eventDateTime);
            int hour_difference = (int) Math.abs(duration.toHours());
            if (hour_difference >= 24) {
                int day_difference = Math.floorDiv(hour_difference, 24);
                time_ago = day_difference + " days ago";
            } else {
                time_ago = hour_difference + " hours ago";
            }
            time_ago_view.setText(time_ago);
        } else {
            time_ago_view.setVisibility(View.GONE);
        }

//        if (moodID != null && !moodID.equals("")) {
//            FirebaseDB.getInstance(requireContext()).getMoodEventFromID(moodID, new FirebaseDB.FirebaseCallback<MoodEvent>() {
//                @Override
//                public void onCallback(MoodEvent moodEvent) {
//                    if (moodEvent != null) {
//                        MoodDetails.this.moodEvent = moodEvent;
//                    } else {
//                        Toast.makeText(requireContext(), "MoodEvent not found, please try again.", Toast.LENGTH_SHORT).show();
//                        requireActivity().finish();
//                    }
//                }
//            });
//        } else if (moodEvent != null) {
//            updateUI();
//        } else {
//            Toast.makeText(requireContext(), "No mood event to find/display, please try again.", Toast.LENGTH_SHORT).show();
//            requireActivity().finish();
//        }

//        ImageView edit_button_view = view.findViewById(R.id.edit_button);
//        if (userProfile.getId().equals(moodEvent.getUserID()) == Boolean.FALSE) {
//            edit_button_view.setVisibility(View.GONE);
//        }
//        edit_button_view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (userProfile == null) {
//                    Toast.makeText(requireContext(), "Cannot load user profile, please try again.", Toast.LENGTH_SHORT).show();
//                } else {
//                    // Launch EditMood logic here
//                }
//            }
//        });

    }

//    public void updateUI() {
//        if (getView() == null || moodEvent == null) {
//            return;
//        }
//        TextView mood_one_view = getView().findViewById(R.id.mood_one);
//        TextView emoji_one_view = getView().findViewById(R.id.emoji_one);
//        TextView time_ago_view = getView().findViewById(R.id.time_ago);
//        ImageView profile_picture_view = getView().findViewById(R.id.profile_picture);
//        TextView first_name_last_name_view = getView().findViewById(R.id.first_name_last_name);
//        TextView profile_username_view = getView().findViewById(R.id.profile_username);
//        TextView with_amount_view = getView().findViewById(R.id.with_amount);
//        TextView mood_description_view = getView().findViewById(R.id.mood_description);
//        ImageView mood_image_view = getView().findViewById(R.id.mood_image);
//        TextView time_view = getView().findViewById(R.id.post_time);
//
//        mood_one_view.setText(moodEvent.getEmotionalState().getName());
//        String date = moodEvent.getFormattedDate();
//        actualDate = moodEvent.getTimestamp().toDate();
//        first_name_last_name_view.setText(fullName);
//        profile_username_view.setText(username);
//        with_amount_view.setText(moodEvent.getSocialSituation());
//        mood_description_view.setText(moodEvent.getTrigger());
//        time_view.setText(date);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            LocalDateTime currentDateTime = LocalDateTime.now();
//            LocalDateTime eventDateTime = LocalDateTime.ofInstant(actualDate.toInstant(), ZoneId.systemDefault());
//            Duration duration = Duration.between(currentDateTime, eventDateTime);
//            int hour_difference = (int) Math.abs(duration.toHours());
//            if (hour_difference >= 24) {
//                int day_difference = Math.floorDiv(hour_difference, 24);
//                time_ago = day_difference + " days ago";
//            } else {
//                time_ago = hour_difference + " hours ago";
//            }
//            time_ago_view.setText(time_ago);
//        } else {
//            time_ago_view.setVisibility(View.GONE);
//        }

        //    image code for onViewCreated
//
//
//    // optional image visibility check
//        if (mood_image_fileurl.equals("")) {
//        mood_image_view.setVisibility(View.GONE);
//    }
//
//    // For photos, we can add Glide to our dependencies, sync gradle, then use the following code:
//            Glide.with(this)
//                    .load(mood_image_fileurl) // Replace this with firebase image url, which should be saved to the database (along with the image itself) during the uploading of the image
//    // .placeholder(R.drawable.placeholder) // optional, we can add a placeholder while loading
//    // .error(R.drawable.error_image) // also optional, we can add an error image if it doesnt load
//                    .into(mood_image_view);
//
//    // doing the same for the profile picture:
//            Glide.with(this)
//                    .load(profile_image_fileurl)
//                    .into(profile_picture_view);

    //}

    public void receiveData(MoodEvent moodEvent) {
        this.moodEvent = moodEvent;
        //updateUI();
    }
}

