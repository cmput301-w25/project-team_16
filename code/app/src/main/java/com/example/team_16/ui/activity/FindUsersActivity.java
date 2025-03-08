package com.example.team_16.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.User;
import com.example.team_16.ui.adapters.UsersAdapter;

import java.util.ArrayList;
import java.util.List;

public class FindUsersActivity extends AppCompatActivity {
    private EditText searchBar;
    private RecyclerView peopleRecyclerView;
    private TextView emptyStateTextView;
    private UsersAdapter adapter;
    private final List<User> userList = new ArrayList<>();
    private User currentUser; // This should be your logged-in user instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);

        // Bind UI elements
        searchBar = findViewById(R.id.search_bar);
        peopleRecyclerView = findViewById(R.id.peopleRecyclerView);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        // Initialize currentUser (in a real app, fetch from your auth/user manager)
        currentUser = new User("currentUserId", "currentUser");
        // Optionally, load the current user's pendingFollow and userFollowing lists from your database

        // Set up RecyclerView and adapter
        adapter = new UsersAdapter(userList, currentUser, user -> {
            // Prevent duplicate requests if already pending or following
            if (currentUser.getPendingFollow().contains(user.getUsername()) ||
                    currentUser.getUserFollowing().contains(user.getUsername())) {
                Toast.makeText(FindUsersActivity.this, "User already followed or pending", Toast.LENGTH_SHORT).show();
                return;
            }
            sendFollowRequest(user);
        });
        peopleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        peopleRecyclerView.setAdapter(adapter);

        // Listen for changes in the search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Search for users using Firebase.
     */
    private void searchUsers(String query) {
        FirebaseDB.getInstance(this).searchUsersByUsername(query, new FirebaseCallback<List<User>>() {
            @Override
            public void onCallback(List<User> result) {
                userList.clear();
                if (result.isEmpty()) {
                    emptyStateTextView.setVisibility(TextView.VISIBLE);
                } else {
                    emptyStateTextView.setVisibility(TextView.GONE);
                    userList.addAll(result);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Sends a follow request to the selected user.
     */
    private void sendFollowRequest(final User targetUser) {
        // Optimistically add to pendingFollow list
        currentUser.getPendingFollow().add(targetUser.getUsername());
        adapter.notifyDataSetChanged();

        FirebaseDB.getInstance(this).sendFollowRequest(currentUser.getId(), targetUser.getId(), new FirebaseCallback<Boolean>() {
            @Override
            public void onCallback(Boolean success) {
                if (success) {
                    Toast.makeText(FindUsersActivity.this, "Follow request sent", Toast.LENGTH_SHORT).show();
                } else {
                    // Revert pendingFollow addition on failure
                    currentUser.getPendingFollow().remove(targetUser.getUsername());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(FindUsersActivity.this, "Error sending follow request", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
