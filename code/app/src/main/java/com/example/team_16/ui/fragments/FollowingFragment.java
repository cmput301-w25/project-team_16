/**
 * FollowingFragment.java
 *
 * Displays a list of users that the currently logged-in user is following.
 *
 * Core Features:
 * - Fetches the list of followed user IDs from Firebase.
 * - For each user ID, fetches user data (username and profile image).
 * - Displays the list using a RecyclerView with the FollowingAdapter.
 * - Allows unfollowing a user directly from the list (removes them visually + updates Firebase).
 * - Supports live search filtering via a search bar.
 * - Shows an empty state message when there are no matching results or no followed users.
 * - Navigates to another user's profile on click using OtherUserProfileFragment.
 *
 * Lifecycle:
 * - onViewCreated initializes UI components, RecyclerView, and loads data.
 * - onResume updates the toolbar title in the parent HomeActivity.
 *
 * Usage:
 * - This fragment is typically hosted inside a container like R.id.fragment_container.
 *
 * Dependencies:
 * - FirebaseDB.java (for retrieving user and following data)
 * - FollowingAdapter.java (handles item rendering and actions)
 * - OtherUserProfileFragment.java (used for profile redirection)
 *
 * Note:
 * Make sure Firebase authentication is complete before showing this fragment.
 */

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
import androidx.fragment.app.FragmentTransaction;
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
    private final List<FollowingAdapter.FollowingItem> originalData = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_following, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseDB = FirebaseDB.getInstance(requireContext());
        currentUserId = firebaseDB.getCurrentUserId();

        if (currentUserId == null) {
            showToast("User not logged in");
            requireActivity().finish();
            return;
        }

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
        adapter = new FollowingAdapter(
                (userId, position) -> firebaseDB.unfollowUser(currentUserId, userId, success -> {
                    if (success) {
                        adapter.removeItem(position);
                        checkEmptyState();
                        showToast("Unfollowed successfully");
                    } else {
                        showToast("Failed to unfollow");
                    }
                }),
                this::openUserProfile
        );
        recyclerView.setAdapter(adapter);
    }

    private void openUserProfile(String userId) {
        Fragment profileFragment = OtherUserProfileFragment.newInstance(userId);

        FragmentTransaction transaction =
                requireActivity().getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        transaction.replace(R.id.fragment_container, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
                    String username = userData != null
                            ? (String) userData.get("username")
                            : "Unknown User";
                    String profileImageUrl = userData != null
                            ? (String) userData.get("profileImageUrl")
                            : null;

                    items.add(new FollowingAdapter.FollowingItem(
                            userId,
                            username,
                            profileImageUrl
                    ));

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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchFilter(s.toString());
            }
        });
    }

    private void applySearchFilter(String query) {
        List<FollowingAdapter.FollowingItem> filtered = new ArrayList<>();
        for (FollowingAdapter.FollowingItem item : originalData) {
            if (item.username != null &&
                    item.username.toLowerCase().contains(query.toLowerCase())) {
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
