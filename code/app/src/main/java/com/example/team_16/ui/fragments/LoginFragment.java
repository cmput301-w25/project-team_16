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
 * This is the login screen of the app.
 * Users can enter their username and password to login.
 * There are also options to sign up and reset password.
 */
public class LoginFragment extends Fragment implements SignUp.SignUpListener {
    // Interface for communication with host activity
    public interface LoginFragmentListener {
        void onLoginSuccess(String userId);
    }

    private LoginFragmentListener listener;

    // UI elements
    private TextInputLayout usernameInputLayout, passwordInputLayout;
    private TextInputEditText usernameEditText, passwordEditText;
    private Button loginButton, signUpButton, resetPasswordButton;
    private LinearLayout loginLinearLayout;
    private FrameLayout fragmentContainer;

    // FirebaseDB instance
    private FirebaseDB firebaseDB;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize Firebase
        firebaseDB = FirebaseDB.getInstance(requireContext());

        // Makes sure the user has to log in / sign up every time
        firebaseDB.logout();

        // Initialize UI elements
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

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            Animation scale_down = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
            loginButton.startAnimation(scale_down);

            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // The next piece of code is to get the red error messages
            boolean hasError = false;

            // Check username field
            if (username.isEmpty()) {
                usernameInputLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Username required"));
                hasError = true;
            } else {
                usernameEditText.setError(null);
                usernameInputLayout.setError(null);
            }
            // Check password field
            if (password.isEmpty()) {
                passwordInputLayout.setError(Html.fromHtml("<font color='#FF0000'>*</font> Password required"));
                hasError = true;
            } else {
                passwordEditText.setError(null);
                passwordInputLayout.setError(null);
            }
            // If any field is empty, show toast and stop processing
            if (hasError) {
                Toast.makeText(requireContext(), "Please enter both username and password!", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseDB.login(username, password, message -> {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                if (message.equals("Login successful!")) {
                    String userId = firebaseDB.getCurrentUserId();

                    // Just notify the activity about successful login with userId
                    if (listener != null) {
                        listener.onLoginSuccess(userId);
                    }
                }
            });
        });

        // Show the SignUp Fragment
        signUpButton.setOnClickListener(v -> {
            // Clear errors on login fields before showing sign-up
            usernameEditText.setError(null);
            passwordEditText.setError(null);

            Animation scale_down = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
            signUpButton.startAnimation(scale_down);

            showNestedFragment(new SignUp());
        });

        // Show the ResetPassword Fragment
        resetPasswordButton.setOnClickListener(v -> {
            // Clear errors on login fields before showing sign-up
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
        // First make the container visible - it needs to be visible for the animation to work
        fragmentContainer.setVisibility(View.VISIBLE);

        // Create the transaction with animations
        FragmentTransaction transaction = getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,  // Fragment enters from right
                        R.anim.slide_out_left,   // Current fragment exits to left
                        R.anim.slide_in_left,    // Fragment enters from left (when popping back stack)
                        R.anim.slide_out_right   // Current fragment exits to right (when popping back stack)
                );

        // Replace any existing fragment and add to back stack
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
     * Callback from SignUp fragment when sign-up is successful
     * This just passes the userId to the activity
     */
    @Override
    public void onSignUpSuccess(String userId) {
        if (listener != null) {
            listener.onLoginSuccess(userId);
        }
    }
}