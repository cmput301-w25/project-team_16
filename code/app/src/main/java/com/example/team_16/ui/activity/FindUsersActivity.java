package com.example.team_16.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_16.R;

import com.example.team_16.models.User;
import com.example.team_16.ui.adapters.UsersAdapter;

import java.util.ArrayList;
import java.util.List;

public class FindUsersActivity extends AppCompatActivity {
    private EditText searchBar;
    private RecyclerView peopleRecyclerView;
    private TextView emptyStateTextView;
    private UsersAdapter adapter;
    private final List<User> userList = new ArrayList<>();
    private User currentUser; // This should be your logged-in user instance


    /**
     * Search for users using Firebase.
     */

}

