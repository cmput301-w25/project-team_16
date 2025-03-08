package com.example.team_16.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.example.team_16.R;

/**
 * Maps fragment displaying location-based mood data
 * with scroll behavior to support hiding the toolbar and bottom navigation.
 */
public class Maps extends Fragment {

    public Maps() {
        // Required empty public constructor
    }

    public static Maps newInstance() {
        return new Maps();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find scrollable content in your maps view
        // This might be a NestedScrollView, ScrollView, or the map itself
        View scrollableContent = view.findViewById(R.id.maps_scroll_container);

        if (scrollableContent instanceof NestedScrollView) {
            // Already a NestedScrollView - good to go
        } else if (scrollableContent instanceof ScrollView) {
            // For regular ScrollView, we need to enable nested scrolling programmatically
            scrollableContent.setNestedScrollingEnabled(true);
        } else {
            // For other views, you may need to wrap them or handle differently
            // depending on your maps implementation
        }

        // TODO: Initialize your map
        // initializeMap();
    }
}