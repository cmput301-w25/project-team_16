package com.example.team_16.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.team_16.ui.adapters.AcceptedFollowersAdapter;
import com.example.team_16.ui.adapters.PendingRequestsAdapter;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FollowRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textNoFollowRequests;
    private AppCompatButton btnAccepted, btnPending;

    private PendingRequestsAdapter pendingAdapter;
    private AcceptedFollowersAdapter acceptedAdapter;

    private FirebaseDB firebaseDB;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_requests);

        firebaseDB = FirebaseDB.getInstance(this);
        currentUserId = firebaseDB.getCurrentUserId();

        initViews();
        setupRecyclerView();

        // Show Pending by default and set the pending button as selected
        btnPending.setSelected(true);
        btnAccepted.setSelected(false);
        showPendingRequests();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_followers);
        textNoFollowRequests = findViewById(R.id.text_no_follow_requests);
        btnAccepted = findViewById(R.id.btn_accepted);
        btnPending = findViewById(R.id.btn_pending);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        btnAccepted.setOnClickListener(v -> {
            btnAccepted.setSelected(true);
            btnPending.setSelected(false);
            showAcceptedFollowers();
        });

        btnPending.setOnClickListener(v -> {
            btnPending.setSelected(true);
            btnAccepted.setSelected(false);
            showPendingRequests();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pending adapter setup
        pendingAdapter = new PendingRequestsAdapter(new PendingRequestsAdapter.OnActionListener() {
            @Override
            public void onAcceptClicked(PendingRequestsAdapter.PendingRequest request, int position) {
                firebaseDB.respondToFollowRequest(request.requestId, true, success -> {
                    if (success) {
                        pendingAdapter.removeItem(position);
                        checkEmptyState();
                    } else {
                        showToast("Failed to accept request");
                    }
                });
            }

            @Override
            public void onRejectClicked(PendingRequestsAdapter.PendingRequest request, int position) {
                firebaseDB.respondToFollowRequest(request.requestId, false, success -> {
                    if (success) {
                        pendingAdapter.removeItem(position);
                        checkEmptyState();
                    } else {
                        showToast("Failed to reject request");
                    }
                });
            }
        });

        // Accepted adapter setup
        acceptedAdapter = new AcceptedFollowersAdapter((follower, position) -> {
            // Remove the follower from current user's followers
            firebaseDB.unfollowUser(follower.userId, currentUserId, success -> {
                if (success) {
                    acceptedAdapter.removeItem(position);
                    checkEmptyState();
                } else {
                    showToast("Failed to remove follower");
                }
            });
        });
    }

    private void showPendingRequests() {
        firebaseDB.getPendingFollowRequests(currentUserId, pendingDocs -> {
            if (pendingDocs == null || pendingDocs.isEmpty()) {
                updatePendingUI(new ArrayList<>());
                return;
            }

            List<PendingRequestsAdapter.PendingRequest> requestList = new ArrayList<>();
            int total = pendingDocs.size();
            final int[] doneCount = {0};

            for (Map<String, Object> doc : pendingDocs) {
                String requestId = (String) doc.get("requestId");
                String fromUserId = (String) doc.get("fromUserId");

                if (requestId == null || fromUserId == null) {
                    incrementCount(total, doneCount, requestList);
                    continue;
                }

                firebaseDB.fetchUserById(fromUserId, userData -> {
                    String fromUsername = userData != null ?
                            (String) userData.get("username") : "Unknown User";

                    requestList.add(new PendingRequestsAdapter.PendingRequest(
                            requestId, fromUserId, fromUsername
                    ));

                    incrementCount(total, doneCount, requestList);
                });
            }
        });
    }

    private void incrementCount(int total, int[] doneCount, List<?> list) {
        doneCount[0]++;
        if (doneCount[0] == total) {
            updatePendingUI(list);
        }
    }

    private void updatePendingUI(List<?> items) {
        runOnUiThread(() -> {
            pendingAdapter.setData((List<PendingRequestsAdapter.PendingRequest>) items);
            recyclerView.setAdapter(pendingAdapter);
            checkEmptyState();
        });
    }

    private void showAcceptedFollowers() {
        firebaseDB.getFollowersOfUser(currentUserId, followerIds -> {
            if (followerIds == null || followerIds.isEmpty()) {
                updateAcceptedUI(new ArrayList<>());
                return;
            }

            List<AcceptedFollowersAdapter.AcceptedFollower> followerList = new ArrayList<>();
            int total = followerIds.size();
            final int[] doneCount = {0};

            for (String followerId : followerIds) {
                firebaseDB.fetchUserById(followerId, userData -> {
                    String username = userData != null ?
                            (String) userData.get("username") : "Unknown User";

                    followerList.add(new AcceptedFollowersAdapter.AcceptedFollower(
                            followerId, username
                    ));

                    doneCount[0]++;
                    if (doneCount[0] == total) {
                        updateAcceptedUI(followerList);
                    }
                });
            }
        });
    }

    private void updateAcceptedUI(List<AcceptedFollowersAdapter.AcceptedFollower> followers) {
        runOnUiThread(() -> {
            acceptedAdapter.setData(followers);
            recyclerView.setAdapter(acceptedAdapter);
            checkEmptyState();
        });
    }

    private void checkEmptyState() {
        runOnUiThread(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            boolean isEmpty = adapter == null || adapter.getItemCount() == 0;

            textNoFollowRequests.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    private void showToast(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        );
    }
}
