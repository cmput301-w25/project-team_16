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
    private TextView emptyStateTextView;
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
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);

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
            emptyStateTextView.setVisibility(View.GONE);
            return;
        }

        currentUser.searchUsersByUsername(query, users -> {
            if (users.isEmpty()) {
                emptyStateTextView.setVisibility(View.VISIBLE);
                peopleRecyclerView.setVisibility(View.GONE);
            } else {
                emptyStateTextView.setVisibility(View.GONE);
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