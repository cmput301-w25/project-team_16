package com.example.team_16;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;

import com.example.team_16.ui.activity.HomeActivity;
import com.example.team_16.ui.fragments.EntryFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EntryFragmentTest {

    @Mock
    private HomeActivity mockContext;

    @Mock
    private LayoutInflater mockInflater;

    @Mock
    private ViewGroup mockContainer;

    @Mock
    private Bundle mockBundle;

    @Mock
    private View mockView;

    @Mock
    private Button mockGetStartedButton;

    @Mock
    private EntryFragment entryFragment;

    @Before
    public void setUp() {
        entryFragment = spy(new EntryFragment());
        when(entryFragment.getActivity()).thenReturn(mockContext);
    }

    @Test
    public void testOnAttach_ValidContext() {
        // Test attaching with a valid context that implements EntryFragmentListener
        entryFragment.onAttach(mockContext);
        // No exception should be thrown
    }

    @Test(expected = ClassCastException.class)
    public void testOnAttach_InvalidContext() {
        // Test attaching with a context that doesn't implement EntryFragmentListener
        Context invalidContext = mock(Context.class);
        entryFragment.onAttach(invalidContext);
    }

    @Test
    public void testOnCreateView() {
        // Mock the view inflation
        when(mockInflater.inflate(anyInt(), any(), anyBoolean())).thenReturn(mockView);
        when(mockView.findViewById(anyInt())).thenReturn(mockGetStartedButton);

        // Create the view
        View result = entryFragment.onCreateView(mockInflater, mockContainer, mockBundle);

        // Verify the view was created
        assertNotNull(result);
        verify(mockInflater).inflate(eq(R.layout.fragment_entry), eq(mockContainer), eq(false));
        verify(mockView).findViewById(R.id.getStartedButton);
    }

    @Test
    public void testOnResume_WithHomeActivity() {
        // Mock the activity
        entryFragment.onAttach(mockContext);

        // Call onResume
        entryFragment.onResume();

        // Verify HomeActivity methods were called
        verify(mockContext).setToolbarTitle("Welcome");
        verify(mockContext).hideBottomNavigation();
        verify(mockContext).makeToolbarUnscrollable();
    }

    @Test
    public void testOnResume_WithoutHomeActivity() {
        // Test onResume when activity is not HomeActivity
        Context invalidContext = mock(Context.class, withSettings().extraInterfaces(EntryFragment.EntryFragmentListener.class));
        entryFragment.onAttach(invalidContext);
        entryFragment.onResume();
        // No exceptions should be thrown
    }

    @Test
    public void testGetStartedButtonClick() {
        // Mock the view inflation
        when(mockInflater.inflate(anyInt(), any(), anyBoolean())).thenReturn(mockView);
        when(mockView.findViewById(anyInt())).thenReturn(mockGetStartedButton);

        // Create the view
        View result = entryFragment.onCreateView(mockInflater, mockContainer, mockBundle);

        // Simulate button click
        mockGetStartedButton.performClick();

        // Verify the button was set up with a click listener
        verify(mockGetStartedButton).setOnClickListener(any());
    }
} 