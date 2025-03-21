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
import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.Comment;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.MoodHistory;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.adapters.CommentAdapter;
import com.google.android.material.imageview.ShapeableImageView;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Fragment responsible for displaying mood details with comments
 */
public class MoodDetails extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private MoodEvent moodEvent;
    private String moodID;
    // UI Elements for mood details
    private TextView mood_one_view;
    private TextView emoji_one_view;
    private TextView time_ago_view;
    private TextView first_name_last_name_view;
    private TextView profile_username_view;
    private TextView with_amount_view;
    private TextView mood_description_view;
    private TextView post_time_view;
    private ImageView gradient_top_view;
    private ConstraintLayout bottom_content_view;
    // UI Elements for comments
    private RecyclerView commentsRecyclerView;
    private TextView commentsHeaderView;
    private TextView noCommentsView;
    private EditText commentInputView;
    private ImageButton sendCommentButton;
    private ShapeableImageView commentProfilePictureView;
    // Comments data
    private List<Comment> commentsList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    // Save original scroll flags to restore them later

    public MoodDetails() {
        // Required empty constructor
    }

    public static MoodDetails newInstance(String param1) {
        MoodDetails fragment = new MoodDetails();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

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
        // Retrieve the list of mood events from the followed users' history
        MoodHistory followingMoodHistory = userProfile.getFollowingMoodHistory();
        if (followingMoodHistory == null) {
            Log.e("MoodDetails", "mood history is null");
        } else {
            Log.d("MoodDetails", "Found mood history with events");
        }
        List<MoodEvent> moodEvents = followingMoodHistory != null ? followingMoodHistory.getAllEvents() : null;
        assert moodEvents != null;
        for (MoodEvent currentMoodEvent : moodEvents) {
            if (currentMoodEvent.getId().equals(moodID)) {
                moodEvent = currentMoodEvent;
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Using the new layout with comments
        return inflater.inflate(R.layout.fragment_mood_details, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (moodEvent == null) {
            Log.e("MoodDetails", "MoodEvent is null");
            Toast.makeText(requireContext(), "Could not load mood details", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get references to the UI elements within the included layout
        View moodDetailsContainer = view.findViewById(R.id.mood_details_container);
        // Find mood detail views
        mood_one_view = moodDetailsContainer.findViewById(R.id.mood_one);
        emoji_one_view = moodDetailsContainer.findViewById(R.id.emoji_one);
        time_ago_view = moodDetailsContainer.findViewById(R.id.time_ago);
        ShapeableImageView profile_picture_view = moodDetailsContainer.findViewById(R.id.profile_picture);
        first_name_last_name_view = moodDetailsContainer.findViewById(R.id.first_name_last_name);
        profile_username_view = moodDetailsContainer.findViewById(R.id.profile_username);
        with_amount_view = moodDetailsContainer.findViewById(R.id.with_amount);
        mood_description_view = moodDetailsContainer.findViewById(R.id.mood_description);
        TextView mood_description2_view = moodDetailsContainer.findViewById(R.id.mood_description2);
        ShapeableImageView mood_image_view = moodDetailsContainer.findViewById(R.id.mood_image);
        post_time_view = moodDetailsContainer.findViewById(R.id.post_time);
        // Find gradient views for styling
        gradient_top_view = moodDetailsContainer.findViewById(R.id.gradient_top);
        bottom_content_view = moodDetailsContainer.findViewById(R.id.bottom_content);
        // Find comment section views
        commentsHeaderView = view.findViewById(R.id.comments_header);
        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        noCommentsView = view.findViewById(R.id.no_comments_text);
        commentInputView = view.findViewById(R.id.comment_input);
        sendCommentButton = view.findViewById(R.id.send_comment_button);
        commentProfilePictureView = view.findViewById(R.id.comment_profile_picture);

        // Apply emotion-based styling
        applyEmotionStyling();
        // Set up mood details data
        displayMoodDetails();
        // Set up comments RecyclerView
        setupCommentsRecyclerView();
        // Load comments for this mood event
        loadComments();
        // Set up comment input functionality
        setupCommentInput();
    }

    /**
     * Apply emotion-specific styling to the UI elements
     */
    private void applyEmotionStyling() {
        if (moodEvent != null) {
            // Apply the emotion text color
            mood_one_view.setTextColor(moodEvent.getEmotionalState().getTextColor());
            mood_description_view.setTextColor(moodEvent.getEmotionalState().getTextColor());
            // Apply the gradient background to the top banner
            if (gradient_top_view != null) {
                gradient_top_view.setImageResource(moodEvent.getEmotionalState().getGradientResourceId());
            }
            // Apply the white background with subtle gradient overlay to the bottom content
            if (bottom_content_view != null) {
                bottom_content_view.setBackgroundResource(moodEvent.getEmotionalState().getBottomGradientResourceId());
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayMoodDetails() {
        // Set emotional state text and emoji
        mood_one_view.setText(moodEvent.getEmotionalState().getName());
        emoji_one_view.setText(moodEvent.getEmotionalState().getEmoji());

        // Set social situation and trigger
        with_amount_view.setText(moodEvent.getSocialSituation());
        mood_description_view.setText(moodEvent.getTrigger());

        // Set formatted date
        String date = moodEvent.getFormattedDate();
        post_time_view.setVisibility(View.VISIBLE);
        post_time_view.setText(date);

        // Set initial loading state for user data
        first_name_last_name_view.setText(R.string.loading);
        profile_username_view.setText("");

        // Fetch user data using callback (same approach as in FeedAdapter)
        FirebaseDB.getInstance(requireContext()).fetchUserById(moodEvent.getUserID(), userData -> {
            if (userData != null) {
                String fullName = (String) userData.get("fullName");
                String username = "@" + userData.get("username");
                first_name_last_name_view.setText(fullName != null ? fullName : "Unknown");
                profile_username_view.setText(username);
            } else {
                first_name_last_name_view.setText(R.string.unknown_user);
                profile_username_view.setText(R.string.unknown);
            }
        });

        // Calculate and display time ago
        Date actualDate = moodEvent.getTimestamp().toDate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime eventDateTime = LocalDateTime.ofInstant(
                    actualDate.toInstant(), ZoneId.systemDefault());
            Duration duration = Duration.between(eventDateTime, currentDateTime);
            int hour_difference = (int) Math.abs(duration.toHours());
            String time_ago;

            if (hour_difference >= 24) {
                int day_difference = hour_difference / 24;
                time_ago = "- " + day_difference + (day_difference == 1 ? " day ago" : " days ago");
            } else if (hour_difference == 0) {
                time_ago = "- Just now";
            } else {
                time_ago = "- " + hour_difference + (hour_difference == 1 ? " hour ago" : " hours ago");
            }
            time_ago_view.setText(time_ago);
        } else {
            time_ago_view.setText("- Recently");
        }
    }

    private void setupCommentsRecyclerView() {
        // Initialize the RecyclerView with a LinearLayoutManager
        commentAdapter = new CommentAdapter(commentsList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentsRecyclerView.setAdapter(commentAdapter);
        // Initially show the "no comments" message
        commentsHeaderView.setText("Comments (0)");
        commentsRecyclerView.setVisibility(View.GONE);
        noCommentsView.setVisibility(View.VISIBLE);
        noCommentsView.setText("No comments yet. Be the first to comment!");
    }

    private void loadComments() {
        // This would normally fetch comments from Firebase
        // For demonstration, let's create a method that would get called when data is fetched
        fetchCommentsFromDatabase();
    }

    private void fetchCommentsFromDatabase() {
        // This would be your actual database fetch logic
        // For example:
        //FirebaseDB.getInstance(requireContext()).fetchCommentsForMood(moodEvent.getId(), commentsList -> {
            // Use the DiffUtil implementation to update the list
        //    updateCommentsList(commentsList);
        //});

        // For now, we'll just work with the empty list
        updateCommentsList(new ArrayList<>());
    }

    private void setupCommentInput() {
        // Setup the comment input area
        commentInputView.setEnabled(true);
        commentInputView.setHint("Add a comment...");
        sendCommentButton.setEnabled(true);
        // Setup the user's profile image in the comment section
        commentProfilePictureView.setImageResource(android.R.drawable.sym_def_app_icon);
        // Set click listener for the send button
        sendCommentButton.setOnClickListener(v -> {
            String commentText = commentInputView.getText().toString().trim();
            if (!commentText.isEmpty()) {
                // In a full implementation, this would add the comment to Firebase
                // For now, we'll just show a toast message
                Toast.makeText(requireContext(), "Comment feature coming soon!", Toast.LENGTH_SHORT).show();
                commentInputView.setText("");
            }
        });
    }

    /**
     * Updates the comments list with new data using DiffUtil for efficient updates
     * @param newComments new list of comments to display
     */
    private void updateCommentsList(List<Comment> newComments) {
        if (newComments == null) {
            newComments = new ArrayList<>();
        }

        // Create a copy of the current list to avoid modification issues
        List<Comment> oldComments = new ArrayList<>(commentsList);

        // Calculate differences
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new CommentDiffCallback(oldComments, newComments)
        );

        // Update data reference
        this.commentsList.clear();
        this.commentsList.addAll(newComments);

        // Dispatch changes to RecyclerView
        diffResult.dispatchUpdatesTo(commentAdapter);

        // Update comments header and visibility
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

    /**
     * DiffUtil callback for efficiently updating the comments list
     */
    private static class CommentDiffCallback extends DiffUtil.Callback {
        private final List<Comment> oldList;
        private final List<Comment> newList;

        public CommentDiffCallback(List<Comment> oldList, List<Comment> newList) {
            this.oldList = oldList != null ? oldList : new ArrayList<>();
            this.newList = newList != null ? newList : new ArrayList<>();
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
            // Compare unique identifiers
            Comment oldComment = oldList.get(oldItemPosition);
            Comment newComment = newList.get(newItemPosition);

            // Assuming Comment has an getId() method, adjust based on your Comment class implementation
            return oldComment.getId().equals(newComment.getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Comment oldComment = oldList.get(oldItemPosition);
            Comment newComment = newList.get(newItemPosition);

            // Compare all relevant fields
            // Adjust these fields based on your Comment class implementation
            boolean sameText = Objects.equals(
                    oldComment.getText(),
                    newComment.getText()
            );

            boolean sameUserId = Objects.equals(
                    oldComment.getUserId(),
                    newComment.getUserId()
            );

            boolean sameTimestamp = Objects.equals(
                    oldComment.getTimestamp(),
                    newComment.getTimestamp()
            );

            return sameText && sameUserId && sameTimestamp;
        }
    }


}