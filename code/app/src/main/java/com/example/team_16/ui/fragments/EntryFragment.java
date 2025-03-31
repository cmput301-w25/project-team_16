package com.example.team_16.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.team_16.R;
import com.example.team_16.ui.activity.HomeActivity;

/**
 * Entry/Onboarding Fragment that shows welcome screen and get started button
 */
public class EntryFragment extends Fragment {
    // Interface for communication with host activity
    public interface EntryFragmentListener {
        void onGetStartedClicked();
    }

    private EntryFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure the host activity implements the callback interface
        try {
            listener = (EntryFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement EntryFragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entry, container, false);

        Button getStartedButton = view.findViewById(R.id.getStartedButton);
        getStartedButton.setOnClickListener(v -> {
            Animation scale_down = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
            getStartedButton.startAnimation(scale_down);

            // Notify the activity through the callback
            if (listener != null) {
                listener.onGetStartedClicked();
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update the UI elements and ensure full screen experience
        if (getActivity() instanceof HomeActivity) {
            HomeActivity activity = (HomeActivity) getActivity();
            activity.setToolbarTitle("Welcome");
            activity.hideBottomNavigation();
            activity.makeToolbarUnscrollable();
        }
    }

    /**
     * Handle back button press - show exit confirmation dialog
     * @return true if handled, false otherwise
     */
    public boolean handleBackPress() {
        // Show exit confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Exit the app
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss dialog
                    dialog.dismiss();
                })
                .create()
                .show();

        // Return true to indicate we've handled the back press
        return true;
    }
}