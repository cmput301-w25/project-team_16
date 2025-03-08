package com.example.team_16.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.team_16.R;
import com.example.team_16.ui.activity.FollowRequestsActivity;

public class Profile extends Fragment {

    public Profile() {
        // Required empty public constructor
    }

    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using the updated fragment_profile.xml
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Reference the root NestedScrollView (with id "fragment_profile")
        NestedScrollView profileScrollView = view.findViewById(R.id.fragment_profile);

        // Setup RecyclerView for mood history
        RecyclerView moodHistoryRecyclerView = view.findViewById(R.id.moodHistoryRecyclerView);
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set click listener for Followers TextView
        TextView followersTextView = view.findViewById(R.id.followersTextView);
        followersTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Launch FollowRequestsActivity when the Followers TextView is clicked
                Intent intent = new Intent(getActivity(), FollowRequestsActivity.class);
                startActivity(intent);
            }
        });

        // Optionally, add a click listener for Following if needed:
        // TextView followingTextView = view.findViewById(R.id.followingTextView);
        // followingTextView.setOnClickListener(...);
    }
}
