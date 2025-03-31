package com.example.team_16.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.team_16.R;
import com.example.team_16.models.UserProfile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Main fragment for monthly mood recap feature.
 * This fragment hosts a ViewPager2 with 5 different pages showing different aspects
 * of the user's monthly mood data.
 */
public class MonthlyMoodRecapFragment extends Fragment {
    private UserProfile userProfile;
    private Calendar lastCompletedMonth;
    private SimpleDateFormat monthYearFormat;
    private TextView tvMonthYear;
    private ViewPager2 viewPager;
    private Button btnBack;
    private Button btnNext;

    // Page fragments
    private MonthlyRecapPage1Fragment page1Fragment;
    private MonthlyRecapPage2Fragment page2Fragment;
    private MonthlyRecapPage3Fragment page3Fragment;
    private MonthlyRecapPage4Fragment page4Fragment;
    private MonthlyRecapPage5Fragment page5Fragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set to last completed month
        lastCompletedMonth = Calendar.getInstance();
        lastCompletedMonth.add(Calendar.MONTH, -1);
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monthly_mood_recap, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        viewPager = view.findViewById(R.id.viewPager);
        btnBack = view.findViewById(R.id.btnBack);
        btnNext = view.findViewById(R.id.btnNext);

        // Set the month title
        updateMonthDisplay();

        // Initialize fragments
        initializeFragments();

        // Setup pagination
        setupViewPager();
        setupPaginationButtons();
    }

    private void initializeFragments() {
        // Create fragments
        page1Fragment = new MonthlyRecapPage1Fragment();
        page2Fragment = new MonthlyRecapPage2Fragment();
        page3Fragment = new MonthlyRecapPage3Fragment();
        page4Fragment = new MonthlyRecapPage4Fragment();
        page5Fragment = new MonthlyRecapPage5Fragment();

        // Set user profile for each fragment
        if (userProfile != null) {
            page1Fragment.setUserProfile(userProfile);
            page2Fragment.setUserProfile(userProfile);
            page3Fragment.setUserProfile(userProfile);
            page4Fragment.setUserProfile(userProfile);
            page5Fragment.setUserProfile(userProfile);
        }
    }

    private void setupViewPager() {
        RecapPagerAdapter adapter = new RecapPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        // Disable swiping between pages to ensure data is loaded properly
        viewPager.setUserInputEnabled(false);
    }

    private void setupPaginationButtons() {
        // Setup button click listeners
        btnBack.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 4) { // 5 pages total (0-4)
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        // Page change callback to update button visibility
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonVisibility(position);
            }
        });

        // Initial button visibility
        updateButtonVisibility(0);
    }

    private void updateButtonVisibility(int position) {
        btnBack.setVisibility(position > 0 ? View.VISIBLE : View.INVISIBLE);
        btnNext.setVisibility(position < 4 ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateMonthDisplay() {
        tvMonthYear.setText(monthYearFormat.format(lastCompletedMonth.getTime()));
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;

        // If fragments are already initialized, update them
        if (page1Fragment != null) {
            page1Fragment.setUserProfile(userProfile);
            page2Fragment.setUserProfile(userProfile);
            page3Fragment.setUserProfile(userProfile);
            page4Fragment.setUserProfile(userProfile);
            page5Fragment.setUserProfile(userProfile);
        }
    }

    /**
     * ViewPager adapter for the monthly recap pages
     */
    private class RecapPagerAdapter extends FragmentStateAdapter {
        public RecapPagerAdapter(FragmentActivity activity) {
            super(activity);
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

        @Override
        public int getItemCount() {
            return 5; // 5 pages total
        }
    }
}