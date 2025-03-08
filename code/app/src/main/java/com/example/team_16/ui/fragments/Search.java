package com.example.team_16.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team_16.R;

public class Search extends Fragment {

    private EditText searchBar;
    private RecyclerView peopleRecyclerView;

    public Search() {
        // Required empty public constructor
    }

    public static Search newInstance() {
        return new Search();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using the provided XML layout
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the EditText and RecyclerView using their IDs in the XML
        searchBar = view.findViewById(R.id.search_bar);
        peopleRecyclerView = view.findViewById(R.id.peopleRecyclerView);
        peopleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        peopleRecyclerView.setNestedScrollingEnabled(true);

        // Set a text watcher on the search bar to handle user input
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed for now
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Perform search as the user types
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed for now
            }
        });
    }

    private void performSearch(String query) {
        // TODO: Implement your search functionality.
        // For example, query your API or database and update the RecyclerView adapter with the results.
    }
}
