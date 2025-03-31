package com.example.team_16.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.team_16.ui.fragments.MonthlyRecapPage1Fragment;
import com.example.team_16.ui.fragments.MonthlyRecapPage2Fragment;
import com.example.team_16.ui.fragments.MonthlyRecapPage3Fragment;
import com.example.team_16.ui.fragments.MonthlyRecapPage4Fragment;
import com.example.team_16.ui.fragments.MonthlyRecapPage5Fragment;

public class MonthlyRecapPagerAdapter extends FragmentStateAdapter {
    private final int NUM_PAGES = 5;

    public MonthlyRecapPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MonthlyRecapPage1Fragment();
            case 1:
                return new MonthlyRecapPage2Fragment();
            case 2:
                return new MonthlyRecapPage3Fragment();
            case 3:
                return new MonthlyRecapPage4Fragment();
            case 4:
                return new MonthlyRecapPage5Fragment();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
} 