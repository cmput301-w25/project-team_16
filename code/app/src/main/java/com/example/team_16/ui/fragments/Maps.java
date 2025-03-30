package com.example.team_16.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.activity.HomeActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Maps extends Fragment
        implements FilterableFragment, FilterFragment.FilterListener, OnMapReadyCallback {

    private UserProfile userProfile;

    private List<MoodEvent> fullMoodEvents;

    private List<MoodEvent> filteredMoodEvents;

    private Map<Marker, MoodEvent> markerEventMap = new HashMap<>();

    private GoogleMap googleMap;

    private TextView noEventSelectedText;
    private LinearLayout eventDetailsLayout;
    private ImageView eventUserProfileImage;
    private TextView eventUsername;
    private TextView eventEmoji;
    private TextView eventLocation;
    private TextView eventTrigger;
    private ImageView eventPhoto;

    private Map<String, String> userNameCache = new HashMap<>();
    private Map<String, String> userImageCache = new HashMap<>();

    private interface OnUserDataFetched {
        void onFetched(String username, String imageUrl);
    }

    public Maps() {
    }

    public static Maps newInstance() {
        return new Maps();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userProfile = ((MoodTrackerApp) requireActivity().getApplication()).getCurrentUserProfile();
        if (userProfile == null) {
            Toast.makeText(requireContext(), "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        List<MoodEvent> personalEvents   = userProfile.getPersonalMoodHistory().getAllEvents();

        List<MoodEvent> followingEvents  = userProfile.getFollowingMoodHistory().getAllEvents();

        List<MoodEvent> combined = new ArrayList<>();
        combined.addAll(personalEvents);
        combined.addAll(followingEvents);

        combined.sort(new Comparator<MoodEvent>() {
            @Override
            public int compare(MoodEvent a, MoodEvent b) {
                if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
                if (a.getTimestamp() == null) return 1;
                if (b.getTimestamp() == null) return -1;
                return b.getTimestamp().compareTo(a.getTimestamp());
            }
        });

        fullMoodEvents = combined;
        filteredMoodEvents = new ArrayList<>(fullMoodEvents);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noEventSelectedText = view.findViewById(R.id.no_event_selected_text);
        eventDetailsLayout  = view.findViewById(R.id.event_details_layout);
        eventUserProfileImage = view.findViewById(R.id.event_user_profile_image);
        eventUsername = view.findViewById(R.id.event_username);
        eventEmoji    = view.findViewById(R.id.event_emoji);
        eventLocation = view.findViewById(R.id.event_location);
        eventTrigger  = view.findViewById(R.id.event_trigger_text);
        eventPhoto    = view.findViewById(R.id.event_photo);

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;

        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setOnMarkerClickListener(marker -> {
            LatLng position = marker.getPosition();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14f));

            MoodEvent clickedEvent = markerEventMap.get(marker);
            if (clickedEvent != null) {
                showEventInBottomContainer(clickedEvent);
            }
            return true;
        });

        updateMapWithEvents(filteredMoodEvents);
    }


    @Override
    public void onFilterClicked() {
        FilterFragment filterFragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putBoolean("show_only_nearby_event_type", true);
        filterFragment.setArguments(args);

        filterFragment.setFilterListener(this);
        ((HomeActivity) requireActivity()).navigateToFragment(filterFragment, "Filter");
    }

    @Override
    public void onApplyFilter(FilterFragment.FilterCriteria criteria) {
        applyFilter(criteria);
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onResetFilter() {
        resetFilters();
    }


    private void applyFilter(FilterFragment.FilterCriteria criteria) {
        List<MoodEvent> filtered = new ArrayList<>();
        Date now = new Date();

        for (MoodEvent event : fullMoodEvents) {
            boolean matches = true;

            if (!criteria.timePeriod.equals("All Time")) {
                if (event.getTimestamp() == null) {
                    matches = false;
                } else {
                    Date eventDate = event.getTimestamp().toDate();
                    long diff = now.getTime() - eventDate.getTime();
                    long days = diff / (1000L * 60 * 60 * 24);

                    if (criteria.timePeriod.equals("Last Year") && days > 365) {
                        matches = false;
                    } else if (criteria.timePeriod.equals("Last Month") && days > 30) {
                        matches = false;
                    } else if (criteria.timePeriod.equals("Last Week") && days > 7) {
                        matches = false;
                    }
                }
            }

            if (matches && criteria.emotionalState != null) {
                if (event.getEmotionalState() == null
                        || !criteria.emotionalState.equalsIgnoreCase(event.getEmotionalState().getName())) {
                    matches = false;
                }
            }

            if (matches && !criteria.triggerReason.isEmpty()) {
                if (event.getTrigger() == null ||
                        !event.getTrigger().toLowerCase().contains(criteria.triggerReason.toLowerCase())) {
                    matches = false;
                }
            }

            if (matches && !event.hasLocation()) {
                matches = false;
            }

            if (matches) {
                filtered.add(event);
            }
        }

        filteredMoodEvents = filtered;
        updateMapWithEvents(filteredMoodEvents);
    }


    private void resetFilters() {
        filteredMoodEvents = new ArrayList<>(fullMoodEvents);
        updateMapWithEvents(filteredMoodEvents);
    }

    private void updateMapWithEvents(List<MoodEvent> events) {
        if (googleMap == null) return;

        googleMap.clear();
        markerEventMap.clear();

        if (events.isEmpty() || !hasLocationEvents(events)) {
            Toast.makeText(requireContext(), "No mood events with location found", Toast.LENGTH_SHORT).show();
            showNoEventSelected();
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasBounds = false;

        for (MoodEvent event : events) {
            if (!event.hasLocation()) continue;

            LatLng pos = new LatLng(event.getLatitude(), event.getLongitude());
            boundsBuilder.include(pos);
            hasBounds = true;

            String moodName = (event.getEmotionalState() != null)
                    ? event.getEmotionalState().getName()
                    : "Unknown";
            BitmapDescriptor icon = createEmojiMarker(getEmojiForMood(moodName));

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(pos)
                    .icon(icon);

            Marker marker = googleMap.addMarker(markerOptions);
            if (marker != null) {
                markerEventMap.put(marker, event);
            }
        }

        if (hasBounds) {
            try {
                int padding = getResources().getDimensionPixelSize(R.dimen.map_padding);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding));
            } catch (Exception e) {
                if (!events.isEmpty() && events.get(0).hasLocation()) {
                    LatLng fallback = new LatLng(
                            events.get(0).getLatitude(), events.get(0).getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fallback, 10f));
                }
            }
        }

        showNoEventSelected();
    }


    private boolean hasLocationEvents(List<MoodEvent> events) {
        for (MoodEvent e : events) {
            if (e.hasLocation()) return true;
        }
        return false;
    }

    private void showNoEventSelected() {
        noEventSelectedText.setVisibility(View.VISIBLE);
        eventDetailsLayout.setVisibility(View.GONE);
        eventPhoto.setVisibility(View.GONE);
    }


    private void showEventInBottomContainer(MoodEvent event) {
        noEventSelectedText.setVisibility(View.GONE);
        eventDetailsLayout.setVisibility(View.VISIBLE);

        String userId = event.getUserID();
        fetchUserData(userId, (username, imageUrl) -> {
            if (username == null || username.isEmpty()) {
                username = "User: " + userId;
            }
            eventUsername.setText(username);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext())
                        .load(imageUrl)
                        .circleCrop()
                        .into(eventUserProfileImage);
            } else {
                eventUserProfileImage.setImageResource(R.drawable.image);
            }
        });

        String moodName = (event.getEmotionalState() != null)
                ? event.getEmotionalState().getName()
                : "Unknown";
        eventEmoji.setText(getEmojiForMood(moodName));

        if (event.getPlaceName() != null && !event.getPlaceName().isEmpty()) {
            eventLocation.setText("Location: " + event.getPlaceName());
        } else if (event.hasLocation()) {
            eventLocation.setText(
                    String.format("Location: (%.4f, %.4f)",
                            event.getLatitude(), event.getLongitude())
            );
        } else {
            eventLocation.setText("Location: None");
        }

        String triggerText = event.getTrigger();
        if (triggerText != null && !triggerText.isEmpty()) {
            eventTrigger.setText("Note: " + triggerText);
        } else {
            eventTrigger.setText("Note: --");
        }

        String photoUrl = event.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            eventPhoto.setVisibility(View.VISIBLE);
            Glide.with(requireContext())
                    .load(photoUrl)
                    .centerCrop()
                    .into(eventPhoto);
        } else {
            eventPhoto.setVisibility(View.GONE);
        }
    }

    private void fetchUserData(String userId, OnUserDataFetched callback) {
        if (userId.equals(userProfile.getId())) {
            callback.onFetched(userProfile.getUsername(), userProfile.getProfileImageUrl());
            return;
        }

        if (userNameCache.containsKey(userId) && userImageCache.containsKey(userId)) {
            callback.onFetched(userNameCache.get(userId), userImageCache.get(userId));
            return;
        }

        userProfile.getFirebaseDB().fetchUserById(userId, userData -> {
            if (userData == null) {
                // not found
                callback.onFetched(null, null);
            } else {
                String fetchedUsername = (String) userData.get("username");
                String fetchedImageUrl = (String) userData.get("profileImageUrl");

                // cache
                userNameCache.put(userId, fetchedUsername);
                userImageCache.put(userId, fetchedImageUrl);

                callback.onFetched(fetchedUsername, fetchedImageUrl);
            }
        });
    }

    private String getEmojiForMood(String mood) {
        if (mood == null) return "❓";
        switch (mood) {
            case "Happiness": return "😊";
            case "Surprise":  return "😱";
            case "Anger":     return "😡";
            case "Confusion": return "😵‍💫";
            case "Disgust":   return "🤢";
            case "Fear":      return "😨";
            case "Sadness":   return "☹️";
            case "Shame":     return "😳";
            default:          return "😐";
        }
    }

    private BitmapDescriptor createEmojiMarker(String emoji) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(60f);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);

        float baseline = -paint.ascent();
        int width = (int) (paint.measureText(emoji) + 0.5f);
        int height = (int) (baseline + paint.descent() + 0.5f);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(emoji, width / 2f, baseline, paint);

        return BitmapDescriptorFactory.fromBitmap(image);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setToolbarTitle("My Mood Map");
        }
    }
}
