package com.example.team_16.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.ui.activity.HomeActivity;
import com.example.team_16.ui.adapters.FollowingAdapter;

import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView textEmpty;
    private EditText searchBar;
    private FollowingAdapter adapter;
    private FirebaseDB firebaseDB;
    private String currentUserId;
    private List<FollowingAdapter.FollowingItem> originalData = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_following, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseDB = FirebaseDB.getInstance(requireContext());
        currentUserId = firebaseDB.getCurrentUserId();

        initializeViews(view);
        setupRecyclerView();
        loadFollowingList();
        setupSearchBar();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_following);
        textEmpty = view.findViewById(R.id.text_empty);
        searchBar = view.findViewById(R.id.search_bar);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FollowingAdapter(new FollowingAdapter.OnRemoveListener() {
            @Override
            public void onRemove(String userId, int position) {
                firebaseDB.unfollowUser(currentUserId, userId, success -> {
                    if (success) {
                        adapter.removeItem(position);
                        checkEmptyState();
                        showToast("Unfollowed successfully");
                    } else {
                        showToast("Failed to unfollow");
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadFollowingList() {
        firebaseDB.getFollowingList(currentUserId, followingIds -> {
            if (followingIds == null || followingIds.isEmpty()) {
                updateUI(new ArrayList<>());
                return;
            }

            List<FollowingAdapter.FollowingItem> items = new ArrayList<>();
            int total = followingIds.size();
            int[] doneCount = {0};

            for (String userId : followingIds) {
                firebaseDB.fetchUserById(userId, userData -> {
                    String username = userData != null ?
                            (String) userData.get("username") : "Unknown User";
                    items.add(new FollowingAdapter.FollowingItem(userId, username));

                    doneCount[0]++;
                    if (doneCount[0] == total) {
                        originalData.clear();
                        originalData.addAll(items);
                        applySearchFilter(searchBar.getText().toString());
                    }
                });
            }
        });
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchFilter(s.toString());
            }
        });
    }

    private void applySearchFilter(String query) {
        List<FollowingAdapter.FollowingItem> filtered = new ArrayList<>();
        for (FollowingAdapter.FollowingItem item : originalData) {
            if (item.username.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(item);
            }
        }
        adapter.setData(filtered);
        checkEmptyState();
    }

    private void updateUI(List<FollowingAdapter.FollowingItem> items) {
        requireActivity().runOnUiThread(() -> {
            adapter.setData(items);
            checkEmptyState();
        });
    }

    private void checkEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        textEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showToast(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) requireActivity()).setToolbarTitle("Following");
    }
}