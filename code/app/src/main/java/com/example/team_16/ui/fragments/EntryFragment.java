/**
 * EntryFragment serves as the welcome/onboarding screen when the app is first launched.
 *
 * Key Features:
 * - Shows a "Get Started" button to guide users to the login/signup flow.
 * - Implements an EntryFragmentListener interface to communicate with the hosting activity (e.g., HomeActivity).
 * - On back press, shows a confirmation dialog to exit the app.
 * - Interacts with the parent HomeActivity to:
 *     - Set the toolbar title to "Welcome"
 *     - Hide the bottom navigation bar
 *     - Make the toolbar unscrollable
 *
 * Usage:
 * This fragment is displayed only on the first launch of the app (determined using SharedPreferences).
 * After "Get Started" is clicked, the app proceeds to LoginFragment.
 */

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


public class EntryFragment extends Fragment {
    public interface EntryFragmentListener {
        void onGetStartedClicked();
    }

    private EntryFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
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

            if (listener != null) {
                listener.onGetStartedClicked();
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

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
        new AlertDialog.Builder(requireContext())
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();

        return true;
    }
}