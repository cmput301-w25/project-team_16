package com.example.team_16.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.team_16.R;
import com.example.team_16.ui.activity.HomeActivity;

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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NestedScrollView profileScrollView = view.findViewById(R.id.fragment_profile);
        RecyclerView moodHistoryRecyclerView = view.findViewById(R.id.moodHistoryRecyclerView);
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView followersTextView = view.findViewById(R.id.followersTextView);
        // In Profile.java's onViewCreated
        followersTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the HomeActivity's navigation method
                ((HomeActivity) requireActivity()).navigateToFragment(
                        new FollowRequestsFragment(),
                        "Follow Requests"  // Set proper title
                );
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) requireActivity()).setToolbarTitle("Profile");
    }
}