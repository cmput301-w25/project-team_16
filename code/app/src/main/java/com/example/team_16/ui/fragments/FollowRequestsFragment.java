/**
 * FollowRequestsFragment.java
 *
 * Displays two tabs: Accepted Followers and Pending Follow Requests for the current user.
 *
 * Functionality:
 * - Users can toggle between viewing:
 *   1. Accepted followers (people who follow the user).
 *   2. Pending requests (people who sent a follow request).
 * - Supports accepting or rejecting incoming follow requests.
 * - Allows the user to remove an accepted follower.
 * - Provides a live search bar to filter results by username.
 * - Displays an empty state message if no results match or no followers/requests exist.
 * - Opens another user's profile when their card is clicked.
 *
 * Lifecycle:
 * - onViewCreated initializes UI, sets up RecyclerView and listeners.
 * - onResume sets the toolbar title using HomeActivity.
 *
 * Dependencies:
 * - FirebaseDB: for accessing user/follow data.
 * - PendingRequestsAdapter & AcceptedFollowersAdapter: for rendering lists and handling clicks.
 * - OtherUserProfileFragment: for viewing another user’s profile.
 *
 * Notes:
 * - `currentUserId` is fetched from FirebaseDB.
 * - Animations are used to enhance tab switching.
 * - Uses ViewModel-like patterns but operates directly with Firebase callbacks.
 */

package com.example.team_16.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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

    private final List<PendingRequestsAdapter.PendingRequest> originalPendingData = new ArrayList<>();
    private final List<AcceptedFollowersAdapter.AcceptedFollower> originalAcceptedData = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_follow_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseDB = FirebaseDB.getInstance(requireContext());
        currentUserId = firebaseDB.getCurrentUserId();

        initViews(view);
        setupRecyclerView();
        btnAccepted.setSelected(true);
        btnPending.setSelected(false);
        showAcceptedFollowers();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_followers);
        textNoFollowRequests = view.findViewById(R.id.text_no_follow_requests);
        btnAccepted = view.findViewById(R.id.btn_accepted);
        btnPending = view.findViewById(R.id.btn_pending);
        searchBar = view.findViewById(R.id.search_bar);

        btnAccepted.setOnClickListener(v -> {
            if (!btnAccepted.isSelected()) {
                btnAccepted.setSelected(true);
                btnPending.setSelected(false);

                Animation scaleUp = AnimationUtils.loadAnimation(
                        requireContext(), R.anim.scale_up
                );
                btnAccepted.startAnimation(scaleUp);

                showAcceptedFollowers();
                applySearchFilter(searchBar.getText().toString());
            }
        });

        btnPending.setOnClickListener(v -> {
            if (!btnPending.isSelected()) {
                btnPending.setSelected(true);
                btnAccepted.setSelected(false);

                Animation scaleUp = AnimationUtils.loadAnimation(
                        requireContext(), R.anim.scale_up
                );
                btnPending.startAnimation(scaleUp);

                showPendingRequests();
                applySearchFilter(searchBar.getText().toString());
            }
        });

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

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        pendingAdapter = new PendingRequestsAdapter(
                (request, position) -> firebaseDB.respondToFollowRequest(request.requestId, true, success -> {
                    if (success) {
                        pendingAdapter.removeItem(position);
                        checkEmptyState();
                    } else {
                        showToast("Failed to accept request");
                    }
                }),
                (request, position) -> firebaseDB.respondToFollowRequest(request.requestId, false, success -> {
                    if (success) {
                        pendingAdapter.removeItem(position);
                        checkEmptyState();
                    } else {
                        showToast("Failed to reject request");
                    }
                }),
                this::openUserProfile
        );

        acceptedAdapter = new AcceptedFollowersAdapter(
                (follower, position) -> firebaseDB.unfollowUser(follower.userId, currentUserId, success -> {
                    if (success) {
                        acceptedAdapter.removeItem(position);
                        checkEmptyState();
                    } else {
                        showToast("Failed to remove follower");
                    }
                }),
                this::openUserProfile
        );

        recyclerView.setAdapter(acceptedAdapter);
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
                    doneCount[0]++;
                    if (doneCount[0] == total) {
                        originalPendingData.clear();
                        originalPendingData.addAll(requestList);
                        applySearchFilter(searchBar.getText().toString());
                    }
                    continue;
                }

                firebaseDB.fetchUserById(fromUserId, userData -> {
                    String fromUsername = userData != null
                            ? (String) userData.get("username")
                            : "Unknown User";
                    String fromUserImageUrl = userData != null
                            ? (String) userData.get("profileImageUrl")
                            : null;

                    requestList.add(new PendingRequestsAdapter.PendingRequest(
                            requestId,
                            fromUserId,
                            fromUsername,
                            fromUserImageUrl
                    ));

                    doneCount[0]++;
                    if (doneCount[0] == total) {
                        originalPendingData.clear();
                        originalPendingData.addAll(requestList);
                        applySearchFilter(searchBar.getText().toString());
                    }
                });
            }
        });
    }

    private void updatePendingUI(List<PendingRequestsAdapter.PendingRequest> items) {
        requireActivity().runOnUiThread(() -> {
            pendingAdapter.setData(items);
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
                    String username = userData != null
                            ? (String) userData.get("username")
                            : "Unknown User";
                    String profileUrl = userData != null
                            ? (String) userData.get("profileImageUrl")
                            : null;

                    followerList.add(new AcceptedFollowersAdapter.AcceptedFollower(
                            followerId,
                            username,
                            profileUrl
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
            List<PendingRequestsAdapter.PendingRequest> filtered = new ArrayList<>();
            for (PendingRequestsAdapter.PendingRequest request : originalPendingData) {
                if (request.fromUsername != null &&
                        request.fromUsername.toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(request);
                }
            }
            pendingAdapter.setData(filtered);
            recyclerView.setAdapter(pendingAdapter);
        } else {
            List<AcceptedFollowersAdapter.AcceptedFollower> filtered = new ArrayList<>();
            for (AcceptedFollowersAdapter.AcceptedFollower follower : originalAcceptedData) {
                if (follower.username != null &&
                        follower.username.toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(follower);
                }
            }
            acceptedAdapter.setData(filtered);
            recyclerView.setAdapter(acceptedAdapter);
        }
        checkEmptyState();
    }

    private void checkEmptyState() {
        requireActivity().runOnUiThread(() -> {
            RecyclerView.Adapter<?> currentAdapter = recyclerView.getAdapter();
            boolean isEmpty = currentAdapter == null || currentAdapter.getItemCount() == 0;

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
        ((HomeActivity) requireActivity()).setToolbarTitle("Followers");
    }
}

