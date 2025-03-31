package com.example.team_16;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.UserProfile;
import com.example.team_16.ui.fragments.Profile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(AndroidJUnit4.class)
public class ProfileFragmentTest {
    private static final String TEST_USER_ID = "test_user_id";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_FULL_NAME = "Test User";
    private static final String TEST_EMAIL = "test@example.com";

    @Mock
    private View mockView;
    @Mock
    private TextView mockUserNameText;
    @Mock
    private TextView mockUserHandleText;
    @Mock
    private Button mockEditProfileButton;
    @Mock
    private FirebaseDB mockFirebaseDB;
    @Mock
    private FragmentManager mockFragmentManager;
    @Mock
    private FragmentTransaction mockFragmentTransaction;

    private Profile profileFragment;
    private UserProfile testUserProfile;

    @Before
    public void setUp() {
        // Initialize with mocked FirebaseDB and required parameters
        testUserProfile = new UserProfile(mockFirebaseDB, TEST_USER_ID, TEST_USERNAME, TEST_FULL_NAME, TEST_EMAIL);

        // Set up fragment
        profileFragment = new Profile();

        // Mock view finding
        when(mockView.findViewById(R.id.userName)).thenReturn(mockUserNameText);
        when(mockView.findViewById(R.id.userHandle)).thenReturn(mockUserHandleText);
        when(mockView.findViewById(R.id.btnEditProfile)).thenReturn(mockEditProfileButton);

        // Mock fragment manager behavior
        when(mockFragmentManager.beginTransaction()).thenReturn(mockFragmentTransaction);
        when(mockFragmentTransaction.replace(anyInt(), any())).thenReturn(mockFragmentTransaction);
    }

    @Test
    public void testProfileDisplay() {
        // Verify view lookups
        verify(mockView).findViewById(R.id.userName);
        verify(mockView).findViewById(R.id.userHandle);
        verify(mockView).findViewById(R.id.btnEditProfile);

        // Verify text setting
        verify(mockUserNameText).setText(TEST_USERNAME);
        verify(mockUserHandleText).setText(TEST_FULL_NAME);
    }
} 