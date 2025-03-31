package com.example.team_16.ui.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.widget.Button;
import android.widget.TextView;

import com.example.team_16.models.UserProfile;

/**
 * Manager class for the Monthly Mood Recap feature.
 * This class coordinates the 5 different pages and handles data loading.
 */
public class MonthlyMoodRecapManager {
    private final FragmentActivity activity;
    private final ViewPager2 viewPager;
    private final Button btnBack;
    private final Button btnNext;

    private final MonthlyRecapPage1Fragment page1Fragment;
    private final MonthlyRecapPage2Fragment page2Fragment;
    private final MonthlyRecapPage3Fragment page3Fragment;
    private final MonthlyRecapPage4Fragment page4Fragment;
    private final MonthlyRecapPage5Fragment page5Fragment;

    public MonthlyMoodRecapManager(
            FragmentActivity activity,
            ViewPager2 viewPager,
            Button btnBack,
            Button btnNext,
            TextView tvMonthYear,
            UserProfile userProfile) {
        this.activity = activity;
        this.viewPager = viewPager;
        this.btnBack = btnBack;
        this.btnNext = btnNext;

        // Initialize fragments
        page1Fragment = new MonthlyRecapPage1Fragment();
        page2Fragment = new MonthlyRecapPage2Fragment();
        page3Fragment = new MonthlyRecapPage3Fragment();
        page4Fragment = new MonthlyRecapPage4Fragment();
        page5Fragment = new MonthlyRecapPage5Fragment();

        // Set user profile for each fragment
        page1Fragment.setUserProfile(userProfile);
        page2Fragment.setUserProfile(userProfile);
        page3Fragment.setUserProfile(userProfile);
        page4Fragment.setUserProfile(userProfile);
        page5Fragment.setUserProfile(userProfile);

        // Setup ViewPager
        setupViewPager();

        // Setup navigation buttons
        setupNavigationButtons();
    }

    private void setupViewPager() {
        RecapPagerAdapter adapter = new RecapPagerAdapter(activity);
        viewPager.setAdapter(adapter);

        // Disable swiping between pages to ensure data is loaded properly
        viewPager.setUserInputEnabled(false);

        // Set initial button states
        updateButtonVisibility(0);
    }

    private void setupNavigationButtons() {
        btnBack.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 4) { // We have 5 pages (0-4)
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonVisibility(position);
            }
        });
    }

    private void updateButtonVisibility(int position) {
        btnBack.setEnabled(position > 0);
        btnNext.setEnabled(position < 4); // We have 5 pages (0-4)
    }

    private class RecapPagerAdapter extends FragmentStateAdapter {
        public RecapPagerAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @Override
        public int getItemCount() {
            return 5; // 5 pages total
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return page1Fragment;
                case 1:
                    return page2Fragment;
                case 2:
                    return page3Fragment;
                case 3:
                    return page4Fragment;
                case 4:
                    return page5Fragment;
                default:
                    return page1Fragment;
            }
        }
    }
}