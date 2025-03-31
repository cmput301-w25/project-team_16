/**
 * LoginFragment.java
 *
 * This fragment handles the login screen for the Mood Tracker app.
 *
 * Core Responsibilities:
 * - Validates user input (username and password).
 * - Displays error messages for empty fields using TextInputLayouts.
 * - Performs login via FirebaseDB and notifies the host activity on success.
 * - Handles transitions to:
 *   - SignUp fragment (for new users)
 *   - ResetPassword fragment (for forgotten passwords)
 *
 * Features:
 * - Animations on button clicks for better UX.
 * - Nested fragment navigation using a FrameLayout.
 * - Clears error states when switching views.
 * - Uses `LoginFragmentListener` to communicate login success to the parent activity.
 * - Implements `SignUp.SignUpListener` to respond to successful sign-ups.
 *
 * UI Elements:
 * - Login form (username + password).
 * - Sign-up and reset password buttons.
 * - `login_fragment_container` used for nested fragment navigation.
 *
 * Usage:
 * The host activity must implement `LoginFragmentListener`.
 */

package com.example.team_16.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.team_16.R;
import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.UserProfile;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment responsible for handling user authentication in the Mood Tracker app.
 * Provides a login form for users to sign in with their username and password.
 * Supports password reset functionality and navigation to the sign-up screen.
 */
public class LoginFragment extends Fragment implements SignUp.SignUpListener {
    /**
     * Interface for handling successful login events
     */
    public interface LoginFragmentListener {
        /**
         * Called when a user successfully logs in
         * @param userId The unique identifier of the logged-in user
         */
        void onLoginSuccess(String userId);
    }

    private LoginFragmentListener listener;

    // UI elements
    private TextInputLayout usernameInputLayout, passwordInputLayout;
    private TextInputEditText usernameEditText, passwordEditText;
    private Button loginButton, signUpButton, resetPasswordButton;
    private LinearLayout loginLinearLayout;
    private FrameLayout fragmentContainer;

    private FirebaseDB firebaseDB;

    /**
     * Called when the fragment is attached to its context
     * Sets up the listener for login success events
     * @param context The context the fragment is being attached to
     * @throws ClassCastException if the context does not implement LoginFragmentListener
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure the host activity implements the callback interface
        try {
            listener = (LoginFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement LoginFragmentListener");
        }
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * Initializes UI components and sets up click listeners.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     * @return The View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        firebaseDB = FirebaseDB.getInstance(requireContext());

        firebaseDB.logout();

        loginLinearLayout = view.findViewById(R.id.loginLinear);
        fragmentContainer = view.findViewById(R.id.login_fragment_container);
        usernameInputLayout = view.findViewById(R.id.usernameInputLayout);
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
        usernameEditText = view.findViewById(R.id.username);
        passwordEditText = view.findViewById(R.id.password);
        loginButton = view.findViewById(R.id.loginButton);
        signUpButton = view.findViewById(R.id.signUpButton);
        resetPasswordButton = view.findViewById(R.id.resetPasswordButton);

        setupClickListeners();

        return view;
    }

    /**
     * Sets up click listeners for all interactive UI elements.
     * Handles login button clicks with validation and Firebase authentication.
     * Manages navigation to sign-up and password reset screens.
     */
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            Animation scale_down = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
            loginButton.startAnimation(scale_down);

            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // The next piece of code is to get the red error messages
            boolean hasError = false;

            if (username.isEmpty()) {
                usernameInputLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Username required"));
                hasError = true;
            } else {
                usernameEditText.setError(null);
                usernameInputLayout.setError(null);
            }
            if (password.isEmpty()) {
                passwordInputLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Password required"));
                hasError = true;
            } else {
                passwordEditText.setError(null);
                passwordInputLayout.setError(null);
            }
            if (hasError) {
                Toast.makeText(requireContext(), "Please enter both username and password!", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseDB.login(username, password, message -> {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                if (message.equals("Login successful!")) {
                    String userId = firebaseDB.getCurrentUserId();

                    if (listener != null) {
                        listener.onLoginSuccess(userId);
                    }
                }
            });
        });

        signUpButton.setOnClickListener(v -> {
            usernameEditText.setError(null);
            passwordEditText.setError(null);

            Animation scale_down = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
            signUpButton.startAnimation(scale_down);

            showNestedFragment(new SignUp());
        });

        resetPasswordButton.setOnClickListener(v -> {
            usernameEditText.setError(null);
            passwordEditText.setError(null);

            Animation scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
            resetPasswordButton.startAnimation(scaleDown);

            showNestedFragment(new ResetPassword());
        });
    }

    /**
     * Show a fragment in the fragment container
     * @param fragment The fragment to show
     */
    private void showNestedFragment(Fragment fragment) {
        fragmentContainer.setVisibility(View.VISIBLE);

        FragmentTransaction transaction = getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                );

        transaction.replace(R.id.login_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Handle back press for child fragments
     * @return true if back press was handled, false otherwise
     */
    public boolean handleBackPress() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    /**
     * Called when a user successfully signs up.
     * Forwards the success event to the parent activity via the login listener.
     *
     * @param userId The unique identifier of the newly created user
     */
    @Override
    public void onSignUpSuccess(String userId) {
        if (listener != null) {
            listener.onLoginSuccess(userId);
        }
    }
}