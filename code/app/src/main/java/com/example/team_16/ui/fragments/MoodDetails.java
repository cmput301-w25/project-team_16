/**
 * MoodDetails.java
 *
 * A Fragment that displays the full details of a specific MoodEvent.
 * It shows information such as:
 * - Mood name and emoji
 * - Who posted the mood, their username, and profile picture
 * - When and where the mood was posted
 * - A description or trigger note
 * - A mood image (if available)
 * - List of comments on the mood
 * - Comment input field for users (except the mood owner)
 *
 * Key Features:
 * - Loads mood data passed by ID through fragment arguments
 * - Fetches mood owner's info and image from Firebase
 * - Dynamically styles the UI based on the selected emotion (color & background)
 * - Allows users (except the mood poster) to comment on the mood
 * - Shows time ago using modern Android time formatting (if supported)
 * - Utilizes RecyclerView and DiffUtil for efficient comment updates
 *
 * Usage:
 * This fragment should be launched using `MoodDetails.newInstance(moodId)`
 * and passed the ID of the mood event to display. It fetches all mood events
 * from the current user's personal and following mood histories to find the target event.
 *
 * Requirements:
 * - UserProfile must be set in the MoodTrackerApp application instance
 * - MoodEvent ID must be passed via arguments
 * - FirebaseDB must be properly initialized to access comments and user data
 *
 * Limitations:
 * - Relies on the presence of all mood events locally in userProfile histories
 * - Only works with Android O and above for precise "time ago" formatting
 */

package com.example.team_16.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.Comment;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.MoodHistory;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.adapters.CommentAdapter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.storage.FirebaseStorage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MoodDetails extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private MoodEvent moodEvent;
    private String moodID;

    private TextView mood_one_view;
    private TextView emoji_one_view;
    private TextView time_ago_view;
    private TextView first_name_last_name_view;
    private TextView profile_username_view;
    private TextView with_amount_view;
    private TextView mood_description_view;
    private TextView mood_description_view2;
    private TextView post_time_view;
    private TextView post_location_view;

    private ShapeableImageView mood_image_view;
    private ImageView gradient_top_view;
    private ConstraintLayout bottom_content_view;
    private RecyclerView commentsRecyclerView;
    private TextView commentsHeaderView;
    private TextView noCommentsView;
    private ShapeableImageView profile_picture_view;
    private EditText commentInputView;
    private ImageButton sendCommentButton;
    private ShapeableImageView commentProfilePictureView;

    private List<Comment> commentsList = new ArrayList<>();
    private CommentAdapter commentAdapter;

    public MoodDetails() {
    }

    /**
     * Creates a new instance of the MoodDetails fragment.
     *
     * @param param1 The ID of the mood event to display
     * @return A new instance of MoodDetails fragment
     */
    public static MoodDetails newInstance(String param1) {
        MoodDetails fragment = new MoodDetails();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes the fragment and loads the mood event data.
     * Retrieves the mood event from either personal or following history.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserProfile userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
        if (getArguments() != null) {
            moodID = getArguments().getString(ARG_PARAM1);
        }
        if (userProfile == null) {
            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        List<MoodEvent> allEvents = new ArrayList<>();
        allEvents.addAll(userProfile.getPersonalMoodHistory().getAllEvents());
        allEvents.addAll(userProfile.getFollowingMoodHistory().getAllEvents());

        for (MoodEvent currentMoodEvent : allEvents) {
            if (currentMoodEvent.getId().equals(moodID)) {
                moodEvent = currentMoodEvent;
                break;
            }
        }
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     * @return The View for the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mood_details, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned.
     * Sets up all UI components and initializes the view state.
     *
     * @param view The View returned by onCreateView()
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (moodEvent == null) {
            Log.e("MoodDetails", "MoodEvent is null");
            Toast.makeText(requireContext(), "Could not load mood details", Toast.LENGTH_SHORT).show();
            return;
        }
        View moodDetailsContainer = view.findViewById(R.id.mood_details_container);

        mood_one_view = moodDetailsContainer.findViewById(R.id.mood_one);
        emoji_one_view = moodDetailsContainer.findViewById(R.id.emoji_one);
        time_ago_view = moodDetailsContainer.findViewById(R.id.time_ago);
        profile_picture_view  = moodDetailsContainer.findViewById(R.id.profile_picture);
        first_name_last_name_view = moodDetailsContainer.findViewById(R.id.first_name_last_name);
        profile_username_view = moodDetailsContainer.findViewById(R.id.profile_username);
        with_amount_view = moodDetailsContainer.findViewById(R.id.with_amount);
        mood_description_view = moodDetailsContainer.findViewById(R.id.mood_description);
        mood_description_view2 = moodDetailsContainer.findViewById(R.id.mood_description2);
        TextView mood_description2_view = moodDetailsContainer.findViewById(R.id.mood_description2);
        mood_image_view = moodDetailsContainer.findViewById(R.id.mood_image);
        post_time_view = moodDetailsContainer.findViewById(R.id.post_time);
        post_location_view = moodDetailsContainer.findViewById(R.id.post_location);

        gradient_top_view = moodDetailsContainer.findViewById(R.id.gradient_top);
        bottom_content_view = moodDetailsContainer.findViewById(R.id.bottom_content);

        commentsHeaderView = view.findViewById(R.id.comments_header);
        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        noCommentsView = view.findViewById(R.id.no_comments_text);
        commentInputView = view.findViewById(R.id.comment_input);
        sendCommentButton = view.findViewById(R.id.send_comment_button);
        commentProfilePictureView = view.findViewById(R.id.comment_profile_picture);

        applyEmotionStyling();

        displayMoodDetails();
        Log.e("log", "harmanOG this works");

        setupCommentsRecyclerView();

        loadComments();

        setupCommentInput();
    }

    /**
     * Applies styling to the UI based on the selected emotional state.
     * Sets colors and gradients for the mood display.
     */
    private void applyEmotionStyling() {
        if (moodEvent != null) {
            mood_one_view.setTextColor(moodEvent.getEmotionalState().getTextColor());
            mood_description_view.setTextColor(moodEvent.getEmotionalState().getTextColor());

            if (gradient_top_view != null) {
                gradient_top_view.setImageResource(moodEvent.getEmotionalState().getGradientResourceId());
            }
            if (bottom_content_view != null) {
                bottom_content_view.setBackgroundResource(moodEvent.getEmotionalState().getBottomGradientResourceId());
            }
        }
    }

    /**
     * Displays all the details of the mood event in the UI.
     * Sets text, images, and visibility of various UI elements.
     */
    @SuppressLint("SetTextI18n")
    private void displayMoodDetails() {

        mood_one_view.setText(moodEvent.getEmotionalState().getName());
        emoji_one_view.setText(moodEvent.getEmotionalState().getEmoji());

        with_amount_view.setText(moodEvent.getSocialSituation());

        if (moodEvent.getTrigger() == "" && moodEvent.getPhotoFilename() != null) {
            mood_description_view.setVisibility(View.GONE);
            mood_description_view2.setVisibility(View.GONE);
        }
        mood_description_view.setText(moodEvent.getTrigger());

        String date = moodEvent.getFormattedDate();
        post_time_view.setVisibility(View.VISIBLE);
        post_time_view.setText(date);

        first_name_last_name_view.setText(R.string.loading);
        profile_username_view.setText("");

        if (moodEvent.getPhotoFilename() != null) {
            mood_image_view.setVisibility(View.VISIBLE);
            FirebaseDB.getInstance(requireContext())
                    .getReference(moodEvent.getPhotoFilename())
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Glide.with(requireContext())
                                .load(uri)
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                                .into(mood_image_view);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    });
        }

        if (moodEvent.getPlaceName() != null && !moodEvent.getPlaceName().isEmpty()) {
            post_location_view.setVisibility(View.VISIBLE);
            post_location_view.setText("- " + moodEvent.getPlaceName());
        } else {
            post_location_view.setVisibility(View.GONE);
        }

        FirebaseDB.getInstance(requireContext())
                .fetchUserById(moodEvent.getUserID(), userData -> {
                    if (userData != null) {
                        String fullName = (String) userData.get("fullName");
                        String userName = (String) userData.get("username");

                        first_name_last_name_view.setText(
                                fullName != null ? fullName : "Unknown");
                        profile_username_view.setText("@" + (userName != null ? userName : "unknown"));

                        String imageUrl = (String) userData.get("profileImageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.image)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(40)))
                                    .into(profile_picture_view);
                        }
                    } else {
                        first_name_last_name_view.setText(R.string.unknown_user);
                        profile_username_view.setText(R.string.unknown);
                    }
                });

        Date actualDate = moodEvent.getTimestamp().toDate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime eventDateTime = LocalDateTime.ofInstant(
                    actualDate.toInstant(), ZoneId.systemDefault());
            Duration duration = Duration.between(eventDateTime, currentDateTime);
            int hour_difference = (int) Math.abs(duration.toHours());
            String time_ago;

//            LOGIC SHOULD WORK FROM HARMAN

            if (hour_difference >= 24) {
                int day_difference = hour_difference / 24;
                time_ago = "- " + day_difference +
                        (day_difference == 1 ? " day ago" : " days ago");
            } else if (hour_difference == 0) {
                time_ago = "- Just now";
            } else {
                time_ago = "- " + hour_difference +
                        (hour_difference == 1 ? " hour ago" : " hours ago");
            }
            time_ago_view.setText(time_ago);
        } else {
            time_ago_view.setText("- Recently");
        }
    }

    /**
     * Sets up the RecyclerView for displaying comments.
     * Initializes the adapter and layout manager.
     */
    private void setupCommentsRecyclerView() {
        UserProfile user = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
        String currentUserId = user != null ? user.getId() : null;

        commentAdapter = new CommentAdapter(commentsList, currentUserId);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentsRecyclerView.setAdapter(commentAdapter);

        commentAdapter.setOnDeleteClickListener(commentId -> {
            FirebaseDB.getInstance(requireContext())
                    .deleteCommentFromMoodEvent(moodEvent.getId(), commentId, success -> {
                        if (success) {
                            loadComments();
                        } else {
                            Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        commentsHeaderView.setText("Comments (0)");
        commentsRecyclerView.setVisibility(View.GONE);
        noCommentsView.setVisibility(View.VISIBLE);
    }

    /**
     * Loads comments for the current mood event from Firebase.
     * Updates the UI with the loaded comments.
     */
    private void loadComments() {
        FirebaseDB.getInstance(requireContext())
                .fetchCommentsForMoodEvent(moodEvent.getId(), fetchedComments -> {
                    updateCommentsList(fetchedComments);
                });
    }

    /**
     * Sets up the comment input field and send button.
     * Handles comment submission and UI updates.
     */
    private void setupCommentInput() {
        UserProfile currentUser = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser.getId().equals(moodEvent.getUserID())) {
            View commentInputContainer = getView().findViewById(R.id.comment_input_container);
            if (commentInputContainer != null) {
                commentInputContainer.setVisibility(View.GONE);
            }
            return;
        }

        if (currentUser.getProfileImageUrl() != null) {
            Glide.with(requireView())
                    .load(currentUser.getProfileImageUrl())
                    .placeholder(R.drawable.image)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(40)))
                    .into(commentProfilePictureView);
        } else {
            commentProfilePictureView.setImageResource(R.drawable.image);
        }

        sendCommentButton.setOnClickListener(v -> {
            String commentText = commentInputView.getText().toString().trim();
            if (!commentText.isEmpty()) {
                addNewComment(commentText);
            }
        });
    }

    private void addNewComment(String commentText) {
        UserProfile userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
        if (userProfile == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment newComment = new Comment(
                userProfile.getId(),
                userProfile.getFullName(),
                commentText
        );
        newComment.setProfileImageUrl(userProfile.getProfileImageUrl());

        FirebaseDB.getInstance(requireContext())
                .addCommentToMoodEvent(moodEvent.getId(), newComment, savedComment -> {
                    if (savedComment == null) {
                        Toast.makeText(requireContext(), "Error posting comment.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Comment posted!", Toast.LENGTH_SHORT).show();
                        commentInputView.setText("");
                        loadComments();
                    }
                });
    }

    /**
     * Updates the comments list with new data.
     * Uses DiffUtil for efficient updates.
     *
     * @param newComments The new list of comments
     */
    private void updateCommentsList(List<Comment> newComments) {
        if (newComments == null) {
            newComments = new ArrayList<>();
        }
        List<Comment> oldComments = new ArrayList<>(commentsList);

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new CommentDiffCallback(oldComments, newComments)
        );

        commentsList.clear();
        commentsList.addAll(newComments);

        diffResult.dispatchUpdatesTo(commentAdapter);

        if (commentsList.isEmpty()) {
            commentsHeaderView.setText("Comments (0)");
            commentsRecyclerView.setVisibility(View.GONE);
            noCommentsView.setVisibility(View.VISIBLE);
        } else {
            commentsHeaderView.setText("Comments (" + commentsList.size() + ")");
            commentsRecyclerView.setVisibility(View.VISIBLE);
            noCommentsView.setVisibility(View.GONE);
        }
    }

    private static class CommentDiffCallback extends DiffUtil.Callback {
        private final List<Comment> oldList;
        private final List<Comment> newList;

        public CommentDiffCallback(List<Comment> oldList, List<Comment> newList) {
            this.oldList = (oldList != null) ? oldList : new ArrayList<>();
            this.newList = (newList != null) ? newList : new ArrayList<>();
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Comment oldComment = oldList.get(oldItemPosition);
            Comment newComment = newList.get(newItemPosition);
            return oldComment.getId().equals(newComment.getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Comment oldComment = oldList.get(oldItemPosition);
            Comment newComment = newList.get(newItemPosition);

            boolean sameText = Objects.equals(oldComment.getText(), newComment.getText());
            boolean sameUserId = Objects.equals(oldComment.getUserId(), newComment.getUserId());
            boolean sameUserName = Objects.equals(oldComment.getUserName(), newComment.getUserName());
            boolean sameTimestamp = (oldComment.getTimestamp() == newComment.getTimestamp());

            return sameText && sameUserId && sameUserName && sameTimestamp;
        }
    }
}
