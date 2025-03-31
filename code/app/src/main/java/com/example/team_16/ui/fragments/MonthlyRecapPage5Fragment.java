package com.example.team_16.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;
import com.example.team_16.ui.adapters.StatItemAdapter;
import com.example.team_16.models.EmotionalState;
import com.example.team_16.models.UserProfile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MonthlyRecapPage5Fragment extends Fragment {
    private static final String TAG = "MonthlyRecapPage5";
    private UserProfile userProfile;
    private Calendar lastCompletedMonth;
    private TextView tvSocialTitle;
    private TextView tvSocialDescription;
    private RecyclerView rvSocialSituations;
    private TextView tvPlaylistTitle;
    private TextView tvPlaylistDescription;
    private Button btnOpenSpotify;
    private EmotionalState topMood;
    private String playlistUrl;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastCompletedMonth = Calendar.getInstance();
        lastCompletedMonth.add(Calendar.MONTH, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monthly_recap_page5, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvSocialTitle = view.findViewById(R.id.tvSocialTitle);
        tvSocialDescription = view.findViewById(R.id.tvSocialDescription);
        rvSocialSituations = view.findViewById(R.id.rvSocialSituations);
        tvPlaylistTitle = view.findViewById(R.id.tvPlaylistTitle);
        tvPlaylistDescription = view.findViewById(R.id.tvPlaylistDescription);
        btnOpenSpotify = view.findViewById(R.id.btnOpenSpotify);

        // Setup RecyclerView
        rvSocialSituations.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set default title and description
        tvSocialTitle.setText("Your Social Connections");
        tvSocialDescription.setText("Here's how different social settings affect your mood");

        // Set default playlist info
        setDefaultPlaylistInfo();

        // Setup playlist button click listener
        btnOpenSpotify.setOnClickListener(v -> {
            openSpotifyPlaylist();
        });

        // If userProfile is already set, update UI
        if (userProfile != null) {
            updateSocialSituations();
        } else {
            // Show empty state
            rvSocialSituations.setAdapter(new StatItemAdapter(
                    new ArrayList<>(),
                    "No social situations recorded this month"
            ));
        }
    }

    private void setDefaultPlaylistInfo() {
        tvPlaylistTitle.setText("Mood Boosters");
        tvPlaylistDescription.setText("Universal feel-good music for your month");
        playlistUrl = "https://open.spotify.com/playlist/37i9dQZF1DX3rxVfibe1L0";
    }

    private void updateSocialSituations() {
        if (userProfile == null) {
            Log.e(TAG, "updateSocialSituations called but userProfile is null");
            return;
        }

        Log.d(TAG, "Fetching monthly stats for social situations...");

        userProfile.getMonthlyStats(
                lastCompletedMonth.get(Calendar.YEAR),
                lastCompletedMonth.get(Calendar.MONTH) + 1,
                stats -> {
                    if (stats == null) {
                        Log.e(TAG, "Monthly stats are null");
                        rvSocialSituations.setAdapter(new StatItemAdapter(
                                new ArrayList<>(),
                                "No social situations recorded this month"
                        ));
                        return;
                    }

                    // Update top mood for playlist recommendations
                    topMood = (EmotionalState) stats.get("topMood");
                    if (topMood != null) {
                        updatePlaylistRecommendation();
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Long> socialBreakdown = (Map<String, Long>) stats.get("socialSituationBreakdown");
                    if (socialBreakdown == null || socialBreakdown.isEmpty()) {
                        Log.e(TAG, "Social situation breakdown is null or empty");
                        rvSocialSituations.setAdapter(new StatItemAdapter(
                                new ArrayList<>(),
                                "No social situations recorded this month"
                        ));
                        return;
                    }

                    Log.d(TAG, "Got social situation breakdown with " + socialBreakdown.size() + " entries");

                    // Sort social situations by count
                    List<Map.Entry<String, Long>> sortedSocialSituations = socialBreakdown.entrySet()
                            .stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                            .collect(Collectors.toList());

                    // Set the adapter with the sorted social situations
                    rvSocialSituations.setAdapter(new StatItemAdapter(
                            sortedSocialSituations,
                            "No social situations recorded this month"
                    ));
                }
        );
    }

    private void updatePlaylistRecommendation() {
        if (topMood == null) return;

        String moodName = topMood.getName();
        String emoji = topMood.getEmoji();

        switch (moodName) {
            case "Happiness":
                tvPlaylistTitle.setText(emoji + " Your Happy Mix");
                tvPlaylistDescription.setText("Upbeat songs to enhance your good mood");
                playlistUrl = "https://open.spotify.com/playlist/2EW2heopW1MzGnC9eZBstm";
                break;
            case "Sadness":
                tvPlaylistTitle.setText(emoji + " Healing Melodies");
                tvPlaylistDescription.setText("Gentle songs for emotional comfort");
                playlistUrl = "https://open.spotify.com/playlist/1OthcfaV5CYgNy6cJLSxIA";
                break;
            case "Fear":
                tvPlaylistTitle.setText(emoji + " Calming Sounds");
                tvPlaylistDescription.setText("Relaxing tracks to ease anxiety and fear");
                playlistUrl = "https://open.spotify.com/playlist/4BmhijQdUAjSrNsi3qbsiP";
                break;
            case "Anger":
                tvPlaylistTitle.setText(emoji + " Empowerment Mix");
                tvPlaylistDescription.setText("Channel frustration into positive energy");
                playlistUrl = "https://open.spotify.com/playlist/1eEGuc58r3qzaRk954rz05";
                break;
            case "Confusion":
                tvPlaylistTitle.setText(emoji + " Clarity Playlist");
                tvPlaylistDescription.setText("Help organize thoughts and clear mental fog");
                playlistUrl = "https://open.spotify.com/playlist/4dMpXT9I0xHBMEOiVUxeLn";
                break;
            case "Disgust":
                tvPlaylistTitle.setText(emoji + " Fresh Start Mix");
                tvPlaylistDescription.setText("Reset your emotions with cleansing sounds");
                playlistUrl = "https://open.spotify.com/playlist/0Yj4sjyDwjgPdNQLOiMD64";
                break;
            case "Shame":
                tvPlaylistTitle.setText(emoji + " Rise Above");
                tvPlaylistDescription.setText("Songs of strength and self-acceptance");
                playlistUrl = "https://open.spotify.com/playlist/2KkLw8SqunAzAm6G3KRjUz";
                break;
            case "Surprise":
                tvPlaylistTitle.setText(emoji + " Unexpected Moments");
                tvPlaylistDescription.setText("Embrace life's surprises with these tracks");
                playlistUrl = "https://open.spotify.com/playlist/37i9dQZF1DX6QdMGVjzAzZ";
                break;
            default:
                setDefaultPlaylistInfo();
                break;
        }
    }

    private void openSpotifyPlaylist() {
        if (playlistUrl == null) {
            playlistUrl = "https://open.spotify.com/playlist/37i9dQZF1DX3rxVfibe1L0"; // Default playlist
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(playlistUrl));

        // Check if Spotify app is installed
        intent.setPackage("com.spotify.music");
        if (intent.resolveActivity(requireContext().getPackageManager()) == null) {
            // Spotify app is not installed, use web browser instead
            intent.setPackage(null);
        }

        startActivity(intent);
    }

    public void setUserProfile(UserProfile userProfile) {
        Log.d(TAG, "setUserProfile called");
        this.userProfile = userProfile;
        if (isAdded() && getView() != null) {
            updateSocialSituations();
        }
    }
}