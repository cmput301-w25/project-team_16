package com.example.team_16.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.example.team_16.MoodTrackerApp;
import com.example.team_16.R;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.activity.HomeActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

    private List<MoodEvent> personalEvents;
    private List<MoodEvent> followingEvents;

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

    private double currentUserLat = Double.NaN;
    private double currentUserLng = Double.NaN;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private boolean firstLoad = true;

    private FusedLocationProviderClient fusedLocationClient;

    private interface OnUserDataFetched {
        void onFetched(String username, String imageUrl);
    }

    public Maps() {}

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

        personalEvents  = userProfile.getPersonalMoodHistory().getAllEvents();
        followingEvents = userProfile.getFollowingMoodHistory().getAllEvents();

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

        noEventSelectedText  = view.findViewById(R.id.no_event_selected_text);
        eventDetailsLayout   = view.findViewById(R.id.event_details_layout);
        eventUserProfileImage= view.findViewById(R.id.event_user_profile_image);
        eventUsername        = view.findViewById(R.id.event_username);
        eventEmoji           = view.findViewById(R.id.event_emoji);
        eventLocation        = view.findViewById(R.id.event_location);
        eventTrigger         = view.findViewById(R.id.event_trigger_text);
        eventPhoto           = view.findViewById(R.id.event_photo);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager()
                .beginTransaction()
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

        centerOnUserLocation(true);
    }

    private void centerOnUserLocation(final boolean showEventsAfterLocating) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION
            );

            if (showEventsAfterLocating) {
                updateMapWithEvents(filteredMoodEvents);
            }
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(requireContext(), "Could not get location", Toast.LENGTH_SHORT).show();
                    if (showEventsAfterLocating) {
                        updateMapWithEvents(filteredMoodEvents);
                    }
                    return;
                }

                android.location.Location location = locationResult.getLastLocation();
                currentUserLat = location.getLatitude();
                currentUserLng = location.getLongitude();

                LatLng userPos = new LatLng(currentUserLat, currentUserLng);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPos, 14f));

                googleMap.addMarker(new MarkerOptions()
                        .position(userPos)
                        .title("My Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                if (showEventsAfterLocating) {
                    updateMapWithEvents(filteredMoodEvents);
                }

                fusedLocationClient.removeLocationUpdates(this);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.getMainLooper());

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentUserLat = location.getLatitude();
                        currentUserLng = location.getLongitude();

                        LatLng userPos = new LatLng(currentUserLat, currentUserLng);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPos, 14f));

                        googleMap.addMarker(new MarkerOptions()
                                .position(userPos)
                                .title("My Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }

                    if (showEventsAfterLocating) {
                        updateMapWithEvents(filteredMoodEvents);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    if (showEventsAfterLocating) {
                        updateMapWithEvents(filteredMoodEvents);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, perms, results);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                centerOnUserLocation(true); // Show events after location found
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                updateMapWithEvents(filteredMoodEvents); // Still show events even without permission
            }
        }
    }
    @Override
    public void onFilterClicked() {
        FilterFragment filterFragment = new FilterFragment();
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
        List<MoodEvent> newFiltered = new ArrayList<>();
        Date now = new Date();

        boolean wantPersonal  = criteria.eventTypes.contains("My Own Mood History");
        boolean wantFollowing = criteria.eventTypes.contains("Events from People I Follow");
        boolean wantNearby    = criteria.eventTypes.contains("Nearby Events within 5km");

        if (wantNearby && (Double.isNaN(currentUserLat) || Double.isNaN(currentUserLng))) {
            Toast.makeText(requireContext(),
                    "Getting your location for nearby events...",
                    Toast.LENGTH_SHORT).show();
            centerOnUserLocation(false);
        }

        boolean noneSelected = (!wantPersonal && !wantFollowing && !wantNearby);
        if (noneSelected) {
            wantPersonal = true;
            wantFollowing = true;
        }

        for (MoodEvent event : fullMoodEvents) {
            boolean matches = false;

            if ((isPersonalEvent(event) && wantPersonal) ||
                    (isFollowingEvent(event) && wantFollowing)) {
                matches = true;
            }

            if (!event.hasLocation()) {
                matches = false;
                continue;
            }

            if (wantNearby && !Double.isNaN(currentUserLat) && !Double.isNaN(currentUserLng)) {
                double distKm = distanceInKm(
                        currentUserLat, currentUserLng,
                        event.getLatitude(), event.getLongitude()
                );
                if (distKm <= 5.0) {
                    matches = true;
                }
            }

            if (!matches) continue;

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

            // emotion
            if (matches && criteria.emotionalState != null && !criteria.emotionalState.isEmpty()) {
                if (event.getEmotionalState() == null
                        || !criteria.emotionalState.equalsIgnoreCase(event.getEmotionalState().getName())) {
                    matches = false;
                }
            }

            // trigger
            if (matches && criteria.triggerReason != null && !criteria.triggerReason.isEmpty()) {
                String eTrig = (event.getTrigger() != null) ? event.getTrigger().toLowerCase() : "";
                if (!eTrig.contains(criteria.triggerReason.toLowerCase())) {
                    matches = false;
                }
            }

            if (matches) {
                newFiltered.add(event);
            }
        }

        filteredMoodEvents = newFiltered;
        firstLoad = false;
        updateMapWithEvents(filteredMoodEvents);
    }

    private void resetFilters() {
        filteredMoodEvents = new ArrayList<>(fullMoodEvents);
        firstLoad = false;
        centerOnUserLocation(true);
    }

    private boolean isPersonalEvent(MoodEvent event) {
        return personalEvents.contains(event);
    }

    private boolean isFollowingEvent(MoodEvent event) {
        return followingEvents.contains(event);
    }

    private void updateMapWithEvents(List<MoodEvent> events) {
        if (googleMap == null) return;

        googleMap.clear();
        markerEventMap.clear();

        if (!Double.isNaN(currentUserLat) && !Double.isNaN(currentUserLng)) {
            LatLng userPos = new LatLng(currentUserLat, currentUserLng);
            googleMap.addMarker(new MarkerOptions()
                    .position(userPos)
                    .title("My Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }

        if (events.isEmpty() || !hasLocationEvents(events)) {
            Toast.makeText(requireContext(), "No mood events with location found", Toast.LENGTH_SHORT).show();
            showNoEventSelected();
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasBounds = false;

        if (!Double.isNaN(currentUserLat) && !Double.isNaN(currentUserLng)) {
            boundsBuilder.include(new LatLng(currentUserLat, currentUserLng));
            hasBounds = true;
        }

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

        if (!firstLoad && hasBounds) {
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
                ? event.getEmotionalState().getName() : "Unknown";
        eventEmoji.setText(getEmojiForMood(moodName));

        if (event.getPlaceName() != null && !event.getPlaceName().isEmpty()) {
            eventLocation.setText("Location: " + event.getPlaceName());
        } else if (event.hasLocation()) {
            eventLocation.setText(
                    String.format("Location: (%.4f, %.4f)",
                            event.getLatitude(), event.getLongitude()));
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
                callback.onFetched(null, null);
            } else {
                String fetchedUsername = (String) userData.get("username");
                String fetchedImageUrl = (String) userData.get("profileImageUrl");

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
        int width  = (int) (paint.measureText(emoji) + 0.5f);
        int height = (int) (baseline + paint.descent() + 0.5f);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(emoji, width / 2f, baseline, paint);

        return BitmapDescriptorFactory.fromBitmap(image);
    }


//     I have used Haversine formula for calculting distance in km

    private double distanceInKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setToolbarTitle("My Mood Map");
        }
    }
}