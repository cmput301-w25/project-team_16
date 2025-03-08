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
import android.widget.SearchView;

import com.example.team_16.R;

/**
 * Search fragment for finding users and moods
 */
public class Search extends Fragment {

    private SearchView searchView;
    private RecyclerView searchResultsRecyclerView;

    public Search() {
        // Required empty public constructor
    }

    public static Search newInstance() {
        return new Search();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the search view
        searchView = view.findViewById(R.id.search_view);

        // Set up RecyclerView for search results
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);
        if (searchResultsRecyclerView != null) {
            searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            // Enable nested scrolling for the RecyclerView
            searchResultsRecyclerView.setNestedScrollingEnabled(true);

            // TODO: Set up your adapter
            // SearchResultsAdapter adapter = new SearchResultsAdapter(getContext());
            // searchResultsRecyclerView.setAdapter(adapter);
        }

        // Set up search listener
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    performSearch(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Optional: update search results as user types
                    return false;
                }
            });
        }
    }

    private void performSearch(String query) {
        // TODO: Implement your search functionality
        // Call your search API or database query
        // Update the RecyclerView with results
    }
}