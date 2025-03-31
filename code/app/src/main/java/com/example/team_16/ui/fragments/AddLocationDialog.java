/**
 * AddLocationDialog is a DialogFragment that allows users to select a location
 * by interacting with a Google Map. Users can either:
 * - Tap on the map to place a marker and select a location
 * - Use the search bar to look up a place by name
 * - Use their current location as a default starting point
 *
 * Key Features:
 * - Shows Google Map with zoom and gesture controls
 * - Supports manual pin placement and geocoding for address retrieval
 * - Uses Geocoder for reverse geocoding and location name display
 * - Notifies the calling fragment or activity via the LocationSelectionListener
 *
 * Usage:
 * Typically used when the user wants to tag a mood event with a specific location.
 * After selection, the selected coordinates and place name are passed back via
 * the LocationSelectionListener interface.
 */

package com.example.team_16.ui.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
    private EditText searchEditText;
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
        searchEditText = view.findViewById(R.id.search_bar);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                handleLocationSearch();
                return true;
            }
            return false;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        cancelButton.setOnClickListener(v -> dismiss());

        saveButton.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(getContext(), "Please select a location.", Toast.LENGTH_SHORT).show();
                return;
            }

            getAddressFromLatLng(selectedLatLng);

            if (locationListener != null) {
                locationListener.onLocationSelected(selectedLatLng, selectedPlaceName);
                dismiss();
            }
        });

        return view;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

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
                selectedPlaceName = address.getAddressLine(0);
            } else {
                selectedPlaceName = "Unknown Location";
            }
        } catch (IOException e) {
            e.printStackTrace();
            selectedPlaceName = "Unknown Location";
        }
    }
    private void handleLocationSearch() {
        String locationQuery = searchEditText.getText().toString().trim();
        if (locationQuery.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a location", Toast.LENGTH_SHORT).show();
            return;
        }
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        searchLocation(locationQuery);
    }
    private void searchLocation(String query) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

                    if (selectedMarker != null) {
                        selectedMarker.remove();
                    }
                    selectedMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Searched Location"));
                    selectedLatLng = latLng;

                    getAddressFromLatLng(latLng);
                }
            } else {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error searching location", Toast.LENGTH_SHORT).show();
        }
    }
}

