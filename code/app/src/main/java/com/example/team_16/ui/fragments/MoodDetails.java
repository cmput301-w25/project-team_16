package com.example.team_16.ui.fragments;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.Comment;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.MoodHistory;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.activity.HomeActivity;
import com.example.team_16.ui.adapters.CommentAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.imageview.ShapeableImageView;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private int originalScrollFlags = -1;
    private AppBarLayout savedAppBarLayout = null;
    private View savedToolbarView = null;

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
    public void onDestroyView() {
        // Show the toolbar again when leaving this fragment
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showToolbar();
            ((HomeActivity) getActivity()).showBottomNavigation();
        }
        // Restore original scroll behavior
        restoreAppBarScrolling();
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Hide the toolbar when this fragment is displayed
        if (getActivity() instanceof HomeActivity) {
            HomeActivity activity = (HomeActivity) getActivity();
            activity.hideBottomNavigation();
            // Disable HomeActivity scrolling by freezing the AppBarLayout
            try {
                // Get the activity's content view properly
                View rootView = activity.findViewById(android.R.id.content);
                if (rootView instanceof ViewGroup) {
                    disableAppBarScrolling((ViewGroup) rootView);
                }
            } catch (Exception e) {
                Log.e("MoodDetails", "Error modifying parent activity layout", e);
            }
        }

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
        // This method would normally fetch comments from Firebase
        // For now, we'll just work with the empty list we created
        // If we had actual comments, we would update the UI like this:
        if (commentsList.isEmpty()) {
            commentsHeaderView.setText("Comments (0)");
            commentsRecyclerView.setVisibility(View.GONE);
            noCommentsView.setVisibility(View.VISIBLE);
        } else {
            commentsHeaderView.setText("Comments (" + commentsList.size() + ")");
            commentsRecyclerView.setVisibility(View.VISIBLE);
            noCommentsView.setVisibility(View.GONE);
            commentAdapter.notifyDataSetChanged();
        }
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
     * Recursively find and disable AppBarLayout scrolling in the parent activity
     */
    private void disableAppBarScrolling(ViewGroup viewGroup) {
        // Look through all children
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            // If we find an AppBarLayout, disable its scrolling
            if (child instanceof AppBarLayout) {
                AppBarLayout appBarLayout = (AppBarLayout) child;
                savedAppBarLayout = appBarLayout; // Save for later restoration
                // Disable scrolling by setting all child views to have no scroll flags
                for (int j = 0; j < appBarLayout.getChildCount(); j++) {
                    View appBarChild = appBarLayout.getChildAt(j);
                    if (appBarChild != null) {
                        AppBarLayout.LayoutParams params =
                                (AppBarLayout.LayoutParams) appBarChild.getLayoutParams();
                        if (params != null) {
                            // Save original flags before removing them
                            originalScrollFlags = params.getScrollFlags();
                            savedToolbarView = appBarChild;
                            params.setScrollFlags(0); // Remove all scroll flags
                            appBarChild.setLayoutParams(params);
                        }
                    }
                }
            }
            // Recursively check any child ViewGroups
            if (child instanceof ViewGroup) {
                disableAppBarScrolling((ViewGroup) child);
            }
        }
    }

    /**
     * Restore the original scrolling behavior of the AppBarLayout
     */
    private void restoreAppBarScrolling() {
        try {
            if (savedAppBarLayout != null && savedToolbarView != null && originalScrollFlags != -1) {
                // Restore original scroll flags
                AppBarLayout.LayoutParams params =
                        (AppBarLayout.LayoutParams) savedToolbarView.getLayoutParams();
                if (params != null) {
                    params.setScrollFlags(originalScrollFlags);
                    savedToolbarView.setLayoutParams(params);
                    Log.d("MoodDetails", "Restored original scroll flags: " + originalScrollFlags);
                }
            } else if (originalScrollFlags == -1) {
                // If we never saved any flags, set default ones
                if (savedAppBarLayout != null && savedToolbarView != null) {
                    AppBarLayout.LayoutParams params =
                            (AppBarLayout.LayoutParams) savedToolbarView.getLayoutParams();
                    if (params != null) {
                        // Common scroll flags: scroll|enterAlways|snap
                        int defaultFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                                AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                                AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP;
                        params.setScrollFlags(defaultFlags);
                        savedToolbarView.setLayoutParams(params);
                        Log.d("MoodDetails", "Set default scroll flags: " + defaultFlags);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MoodDetails", "Error restoring AppBarLayout scrolling", e);
        }
    }
}