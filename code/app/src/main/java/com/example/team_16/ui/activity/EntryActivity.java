package com.example.team_16.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.team_16.R;

public class EntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Fade());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        Button getStartedButton = findViewById(R.id.getStartedButton);
        getStartedButton.setOnClickListener(v -> {
            Animation scale_down = AnimationUtils.loadAnimation(this, R.anim.scale_down);

            getStartedButton.startAnimation(scale_down);
            startActivity(new Intent(EntryActivity.this, MainActivity.class));

            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
    }
}