package com.example.team_16.ui.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import com.example.team_16.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddLocationDialog extends DialogFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker selectedMarker;
    private LatLng selectedLatLng;
    private String selectedPlaceName;
    private Button saveButton, cancelButton;
    private FusedLocationProviderClient fusedLocationClient;


    public interface LocationSelectionListener {
        void onLocationSelected(LatLng latLng, String placeName);
    }

    private LocationSelectionListener locationListener;

    public AddLocationDialog(LocationSelectionListener listener) {
        this.locationListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_location, container, false);

        // Initialize buttons
        saveButton = view.findViewById(R.id.save_location_button);
        cancelButton = view.findViewById(R.id.cancel_location_button);

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Cancel button (dismiss dialog without saving)
        cancelButton.setOnClickListener(v -> dismiss());

        // Save button (return data to AddMood class)
        saveButton.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(getContext(), "Please select a location.", Toast.LENGTH_SHORT).show();
                return;
            }

            getAddressFromLatLng(selectedLatLng);

            if (locationListener != null) {
                // Send the selected location back
                locationListener.onLocationSelected(selectedLatLng, selectedPlaceName);
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom controls and gestures
        mMap.getUiSettings().setZoomControlsEnabled(true); // Show zoom buttons (+/-)
        mMap.getUiSettings().setZoomGesturesEnabled(true); //  Allow pinch-to-zoom
        mMap.getUiSettings().setScrollGesturesEnabled(true); // Allow scrolling
        mMap.getUiSettings().setRotateGesturesEnabled(true); // Allow rotation

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                    selectedLatLng = userLatLng;
                    selectedMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
                    getAddressFromLatLng(userLatLng);
                } else {
                    Toast.makeText(getContext(), "Could not get current location.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Location permission error.", Toast.LENGTH_SHORT).show();
        }

        mMap.setOnMapClickListener(latLng -> {
            if (selectedMarker != null) {
                selectedMarker.remove();
            }
            selectedLatLng = latLng;
            selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            getAddressFromLatLng(latLng);
        });
    }


    /**
     * Uses Geocoder to retrieve a place name from latitude and longitude.
     *
     * @param latLng The selected location coordinates.
     */
    public void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedPlaceName = address.getAddressLine(0);  // Get full address
            } else {
                selectedPlaceName = "Unknown Location";
            }
        } catch (IOException e) {
            e.printStackTrace();
            selectedPlaceName = "Unknown Location";
        }
    }
}

