package com.example.team_16.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.team_16.R;

/**
 * Feed fragment that displays the user's mood feed with scroll behavior
 * to support hiding the toolbar and bottom navigation.
 */
public class Feed extends Fragment {
    private RecyclerView recyclerView;

    public Feed() {
        // Required empty public constructor
    }

    public static Feed newInstance() {
        return new Feed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView for scrolling behavior
        recyclerView = view.findViewById(R.id.feed_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Important: Enable nested scrolling for scroll behavior to work with CoordinatorLayout
        recyclerView.setNestedScrollingEnabled(true);

        // TODO: Set up your adapter
        // FeedAdapter adapter = new FeedAdapter(getContext(), feedItems);
        // recyclerView.setAdapter(adapter);
    }
}