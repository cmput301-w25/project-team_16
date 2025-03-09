package com.example.team_16.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.ui.activity.HomeActivity;
import com.example.team_16.ui.adapters.AcceptedFollowersAdapter;
import com.example.team_16.ui.adapters.PendingRequestsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FollowRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView textNoFollowRequests;
    private AppCompatButton btnAccepted, btnPending;
    private EditText searchBar;

    private PendingRequestsAdapter pendingAdapter;
    private AcceptedFollowersAdapter acceptedAdapter;

    private FirebaseDB firebaseDB;
    private String currentUserId;

    private List<PendingRequestsAdapter.PendingRequest> originalPendingData = new ArrayList<>();
    private List<AcceptedFollowersAdapter.AcceptedFollower> originalAcceptedData = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_follow_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseDB = FirebaseDB.getInstance(requireContext());
        currentUserId = firebaseDB.getCurrentUserId();

        initViews(view);
        setupRecyclerView();

        // Show Pending by default
        btnPending.setSelected(true);
        btnAccepted.setSelected(false);
        showPendingRequests();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_followers);
        textNoFollowRequests = view.findViewById(R.id.text_no_follow_requests);
        btnAccepted = view.findViewById(R.id.btn_accepted);
        btnPending = view.findViewById(R.id.btn_pending);
        searchBar = view.findViewById(R.id.search_bar);



        btnAccepted.setOnClickListener(v -> {
            btnAccepted.setSelected(true);
            btnPending.setSelected(false);
            showAcceptedFollowers();
            applySearchFilter(searchBar.getText().toString());
        });

        btnPending.setOnClickListener(v -> {
            btnPending.setSelected(true);
            btnAccepted.setSelected(false);
            showPendingRequests();
            applySearchFilter(searchBar.getText().toString());
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchFilter(s.toString());
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

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

        acceptedAdapter = new AcceptedFollowersAdapter((follower, position) -> {
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
            originalPendingData.clear();
            originalPendingData.addAll((List<PendingRequestsAdapter.PendingRequest>) list);
            applySearchFilter(searchBar.getText().toString());
        }
    }

    private void updatePendingUI(List<?> items) {
        requireActivity().runOnUiThread(() -> {
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
                        originalAcceptedData.clear();
                        originalAcceptedData.addAll(followerList);
                        applySearchFilter(searchBar.getText().toString());
                    }
                });
            }
        });
    }

    private void updateAcceptedUI(List<AcceptedFollowersAdapter.AcceptedFollower> followers) {
        requireActivity().runOnUiThread(() -> {
            acceptedAdapter.setData(followers);
            recyclerView.setAdapter(acceptedAdapter);
            checkEmptyState();
        });
    }

    private void applySearchFilter(String query) {
        if (btnPending.isSelected()) {
            filterPendingRequests(query);
        } else {
            filterAcceptedFollowers(query);
        }
    }

    private void filterPendingRequests(String query) {
        List<PendingRequestsAdapter.PendingRequest> filtered = new ArrayList<>();
        for (PendingRequestsAdapter.PendingRequest request : originalPendingData) {
            if (request.fromUsername.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(request);
            }
        }
        pendingAdapter.setData(filtered);
        checkEmptyState();
    }

    private void filterAcceptedFollowers(String query) {
        List<AcceptedFollowersAdapter.AcceptedFollower> filtered = new ArrayList<>();
        for (AcceptedFollowersAdapter.AcceptedFollower follower : originalAcceptedData) {
            if (follower.username.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(follower);
            }
        }
        acceptedAdapter.setData(filtered);
        checkEmptyState();
    }

    private void checkEmptyState() {
        requireActivity().runOnUiThread(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            boolean isEmpty = adapter == null || adapter.getItemCount() == 0;

            textNoFollowRequests.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    private void showToast(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        );
    }
    @Override
    public void onResume() {
        super.onResume();
        // Ensure title is correct even after rotation
        ((HomeActivity) requireActivity()).setToolbarTitle("Followers");
    }
}