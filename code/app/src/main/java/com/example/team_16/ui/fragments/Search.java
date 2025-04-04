/**
 * Search.java
 *
 * A fragment that allows users to search for other users by their username,
 * and follow or unfollow them directly from the search results.
 *
 * Key Features:
 * - Uses FirebaseDB to fetch matching usernames.
 * - Filters out the current user from the results.
 * - Allows users to send follow requests or unfollow directly.
 * - Integrates with a `SearchAdapter` to handle UI interactions.
 * - Displays a message if no users are found.
 *
 * UI Components:
 * - `EditText searchBar`: for typing in the search query.
 * - `RecyclerView peopleRecyclerView`: displays matched users.
 * - `TextView noSearchMessage`: shown when there are no results.
 *
 * Interaction Flow:
 * - On text change in `searchBar`, performs a real-time search.
 * - Search results are updated via the `SearchAdapter`.
 * - Follow/unfollow actions are triggered via the adapter callbacks.
 * - Clicking on a user opens their profile using `OtherUserProfileFragment`.
 *
 * Dependencies:
 * - `FirebaseDB`: handles backend user lookup and follow/unfollow operations.
 * - `UserProfile`: provides current user's data and methods for following users.
 * - `SearchAdapter`: custom adapter to display search results with interaction support.
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.adapters.SearchAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Search extends Fragment implements SearchAdapter.OnFollowClickListener {

    private EditText searchBar;
    private RecyclerView peopleRecyclerView;
    private TextView noSearchMessage;
    private SearchAdapter adapter;
    private UserProfile currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseDB firebaseDB = FirebaseDB.getInstance(requireContext());
        String currentUserId = firebaseDB.getCurrentUserId();

        searchBar = view.findViewById(R.id.search_bar);
        peopleRecyclerView = view.findViewById(R.id.peopleRecyclerView);
        noSearchMessage = view.findViewById(R.id.noSearchMessage);

        peopleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchAdapter(this);
        peopleRecyclerView.setAdapter(adapter);

        UserProfile.loadFromFirebase(firebaseDB, currentUserId, profile -> {
            if (profile != null) {
                currentUser = profile;
                updateAdapterLists();
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }
        });

        noSearchMessage.setVisibility(View.VISIBLE);
        peopleRecyclerView.setVisibility(View.GONE);
    }

    private void updateAdapterLists() {
        adapter.updateLists(
                currentUser.getUserFollowing(),
                currentUser.getPendingFollow()
        );
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            adapter.setUsers(new ArrayList<>());
            noSearchMessage.setVisibility(View.VISIBLE);
            peopleRecyclerView.setVisibility(View.GONE);
            return;
        }

        noSearchMessage.setVisibility(View.GONE);
        peopleRecyclerView.setVisibility(View.VISIBLE);

        currentUser.searchUsersByUsername(query, users -> {
            if (users.isEmpty()) {
                noSearchMessage.setText("❌\nNo users found");
                noSearchMessage.setVisibility(View.VISIBLE);
                peopleRecyclerView.setVisibility(View.GONE);
            } else {
                noSearchMessage.setVisibility(View.GONE);
                peopleRecyclerView.setVisibility(View.VISIBLE);
                filterAndDisplayUsers(users);
            }
        });
    }

    private void filterAndDisplayUsers(List<Map<String, Object>> users) {
        List<Map<String, Object>> filteredUsers = new ArrayList<>();
        String currentUserId = currentUser.getId();

        for (Map<String, Object> user : users) {
            String userId = (String) user.get("id");
            if (!userId.equals(currentUserId)) {
                filteredUsers.add(user);
            }
        }
        adapter.setUsers(filteredUsers);
    }

    @Override
    public void onUserClick(String targetUserId) {
        goToUserProfile(targetUserId);
    }

    private void goToUserProfile(String targetUserId) {
        OtherUserProfileFragment fragment = OtherUserProfileFragment.newInstance(targetUserId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
    // end new code

    @Override
    public void onFollowClick(String targetUserId) {
        currentUser.sendFollowRequest(targetUserId, success -> {
            if (success) {
                updateAdapterLists();
                Toast.makeText(getContext(), "Follow request sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to send request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUnfollowClick(String targetUserId) {
        currentUser.unfollowUser(targetUserId, success -> {
            if (success) {
                currentUser.refreshFollowData(() -> {
                    updateAdapterLists();
                    Toast.makeText(getContext(), "Unfollowed successfully", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(getContext(), "Failed to unfollow", Toast.LENGTH_SHORT).show();
            }
        });
    }
}