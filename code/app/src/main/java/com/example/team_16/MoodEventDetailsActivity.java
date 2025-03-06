package com.example.team_16;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;


/*
*  The purpose of this activity within the app is to allow users to view the details of any
* mood event when clicked upon.
* If done so, this activity should then give the option to edit the mood event details, sending
* the user to another activity.
* When this is completed, the second activity should give this activity the changes using an intent
* which should then cause the screen to be updated accordingly
* */
public class MoodEventDetailsActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> editMoodEventLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moodeventdetails);

        Intent intent = getIntent();
        
        TextView mood_one_view = findViewById(R.id.mood_one);
        TextView mood_two_view = findViewById(R.id.mood_two);
        TextView emoji_one_view = findViewById(R.id.emoji_one);
        TextView emoji_two_view = findViewById(R.id.emoji_two);
        TextView time_ago_view = findViewById(R.id.time_ago);
        ImageView profile_picture_view = findViewById(R.id.profile_picture);
        TextView first_name_last_name_view = findViewById(R.id.first_name_last_name);
        TextView profile_username_view = findViewById(R.id.profile_username);
        TextView with_amount_view = findViewById(R.id.with_amount);
        TextView mood_description_view = findViewById(R.id.mood_description);
        ImageView mood_image_view = findViewById(R.id.mood_image);
        TextView time_view = findViewById(R.id.post_time);
        ImageView edit_button_view = findViewById(R.id.edit_button);
        ImageView back_button_view = findViewById(R.id.back_button);

        String happy_emoji = "😊";
        String surprised_emoji = "😱";
        String angry_emoji = "😡";
        String confused_emoji = "😵‍💫";
        String disgust_emoji = "🤢";
        String fear_emoji = "😨";
        String sad_emoji = "☹️";
        String shame_emoji = "😳";

        String mood_one = intent.getStringExtra("Mood_One");
        String mood_two = intent.getStringExtra("Mood_Two");
        String first_name_last_name = intent.getStringExtra("First_Name_Last_Name");
        String username = intent.getStringExtra("Username");
        String with_amount = intent.getStringExtra("With_Amount");
        String mood_description = intent.getStringExtra("Mood_Description");
        String time = intent.getStringExtra("Time");
        String date = intent.getStringExtra("Date");
        String location = intent.getStringExtra("Location");
        String mood_image_fileurl = intent.getStringExtra("Mood_Image_URL");
        String profile_image_fileurl = intent.getStringExtra("Profile_Image_URL");


        switch (mood_one) {
            case "Happy":
                emoji_one_view.setText(happy_emoji);
                break;
            case "Surprised":
                emoji_one_view.setText(surprised_emoji);
                break;
            case "Angry":
                emoji_one_view.setText(angry_emoji);
                break;
            case "Confused":
                emoji_one_view.setText(confused_emoji);
                break;
            case "Disgust":
                emoji_one_view.setText(disgust_emoji);
                break;
            case "Fear":
                emoji_one_view.setText(fear_emoji);
                break;
            case "Sad":
                emoji_one_view.setText(sad_emoji);
                break;
            case "Shame":
                emoji_one_view.setText(shame_emoji);
                break;
        }

        switch (mood_two) {
            case "Happy":
                emoji_two_view.setText(happy_emoji);
                break;
            case "Surprised":
                emoji_two_view.setText(surprised_emoji);
                break;
            case "Angry":
                emoji_two_view.setText(angry_emoji);
                break;
            case "Confused":
                emoji_two_view.setText(confused_emoji);
                break;
            case "Disgust":
                emoji_two_view.setText(disgust_emoji);
                break;
            case "Fear":
                emoji_two_view.setText(fear_emoji);
                break;
            case "Sad":
                emoji_two_view.setText(sad_emoji);
                break;
            case "Shame":
                emoji_two_view.setText(shame_emoji);
                break;
        }

        mood_one_view.setText(mood_one);
        mood_two_view.setText(mood_two);

        String time_ago = "N/A";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            String[] date_values = date.split("-");
            String[] time_values = time.split(":");
            LocalDateTime moodDate = LocalDateTime.of(Integer.parseInt(date_values[0]), Integer.parseInt(date_values[1]), Integer.parseInt(date_values[2]), Integer.parseInt(time_values[0]), Integer.parseInt(time_values[1]));
            Duration duration = Duration.between(currentDateTime, moodDate);
            int hour_difference = (int) Math.abs(duration.toHours());
                if (hour_difference >= 24) {
                    int day_difference = Math.floorDiv(hour_difference, 24);
                    time_ago = day_difference + " days ago";
                }
                else {
                    time_ago = hour_difference + " hours ago";
                }
            }
            else {
                time_ago_view.setVisibility(View.GONE);
            }

        time_ago_view.setText(time_ago);
        profile_picture_view.setImageResource(android.R.drawable.sym_def_app_icon); // example profile picture
        first_name_last_name_view.setText(first_name_last_name);
        profile_username_view.setText("@" + username);
        with_amount_view.setText("With: " + with_amount);
        mood_description_view.setText(mood_description);
        Drawable mood_image_drawable = getResources().getDrawable(R.drawable.dog); //example image of dog
        mood_image_view.setImageDrawable(mood_image_drawable);
        time_view.setText(time + "  " + date + "  " + location);

        // optional image visibility check
        if (mood_image_fileurl.equals("")) {
            mood_image_view.setVisibility(View.GONE);
        }

        // For photos, we can add Glide to our dependencies, sync gradle, then use the following code:
            Glide.with(this)
                    .load(mood_image_fileurl) // Replace this with firebase image url, which should be saved to the database (along with the image itself) during the uploading of the image
                    // .placeholder(R.drawable.placeholder) // optional, we can add a placeholder while loading
                    // .error(R.drawable.error_image) // also optional, we can add an error image if it doesnt load
                    .into(mood_image_view);

        // doing the same for the profile picture:
            Glide.with(this)
                    .load(profile_image_fileurl)
                    .into(profile_picture_view);


        // back button, returns to previous activity
        back_button_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go back to the previous activity in the stack
                finish();
            }
        });
        
        // The following code can be uncommented and tested once the EditMoodEvent activity is created

//        edit_button_view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                //go to edit mood event activity when edit button is pressed
//                Intent intent2 = new Intent(MoodEventDetailsActivity.this, EditMoodEventActivity.class);
//                intent2.putExtra("Mood_One", mood_one);
//                intent2.putExtra("Mood_Two", mood_two);
//                intent2.putExtra("Time_Ago", time_ago);
//                intent2.putExtra("First_Name_Last_Name", first_name_last_name);
//                intent2.putExtra("Username", username);
//                intent2.putExtra("With_Amount", with_amount);
//                intent2.putExtra("Mood_Description", mood_description);
//                intent2.putExtra("Mood_Image", mood_image_fileurl);
//
//                editMoodEventLauncher.launch(intent);
//
//            }
//        });
//
//        // Once returned from the edit activity menu, check the results and redisplay info
//        editMoodEventLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                        Intent data = result.getData();
//                        mood_one_view.setText(data.getStringExtra("Mood_One"));
//                        mood_two_view.setText(data.getStringExtra("Mood_Two"));
//                        first_name_last_name_view.setText(data.getStringExtra("First_Name_Last_Name"));
//                        profile_username_view.setText("@" + data.getStringExtra("Username"));
//                        with_amount_view.setText(data.getStringExtra("With_Amount"));
//                        mood_description_view.setText(data.getStringExtra("Mood_Description"));
//
//                        switch (data.getStringExtra("Mood_One")) {
//                            case "Happy":
//                                emoji_one_view.setText(happy_emoji);
//                                break;
//                            case "Surprised":
//                                emoji_one_view.setText(surprised_emoji);
//                                break;
//                            case "Angry":
//                                emoji_one_view.setText(angry_emoji);
//                                break;
//                            case "Confused":
//                                emoji_one_view.setText(confused_emoji);
//                                break;
//                            case "Disgust":
//                                emoji_one_view.setText(disgust_emoji);
//                                break;
//                            case "Fear":
//                                emoji_one_view.setText(fear_emoji);
//                                break;
//                            case "Sad":
//                                emoji_one_view.setText(sad_emoji);
//                                break;
//                            case "Shame":
//                                emoji_one_view.setText(shame_emoji);
//                                break;
//                        }
//
//                        switch (data.getStringExtra("Mood_Two")) {
//                            case "Happy":
//                                emoji_two_view.setText(happy_emoji);
//                                break;
//                            case "Surprised":
//                                emoji_two_view.setText(surprised_emoji);
//                                break;
//                            case "Angry":
//                                emoji_two_view.setText(angry_emoji);
//                                break;
//                            case "Confused":
//                                emoji_two_view.setText(confused_emoji);
//                                break;
//                            case "Disgust":
//                                emoji_two_view.setText(disgust_emoji);
//                                break;
//                            case "Fear":
//                                emoji_two_view.setText(fear_emoji);
//                                break;
//                            case "Sad":
//                                emoji_two_view.setText(sad_emoji);
//                                break;
//                            case "Shame":
//                                emoji_two_view.setText(shame_emoji);
//                                break;
//                        }
//
//                        String time_ago2 = "N/A";
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            LocalDateTime currentDateTime = LocalDateTime.now();
//                            String[] date_values = (data.getStringExtra("Date")).split("-");
//                            String[] time_values = (data.getStringExtra("Time")).split(":");
//                            LocalDateTime moodDate = LocalDateTime.of(Integer.parseInt(date_values[0]), Integer.parseInt(date_values[1]), Integer.parseInt(date_values[2]), Integer.parseInt(time_values[0]), Integer.parseInt(time_values[1]));
//                            Duration duration = Duration.between(currentDateTime, moodDate);
//                            int hour_difference = (int) Math.abs(duration.toHours());
//                            if (hour_difference >= 24) {
//                                int day_difference = Math.floorDiv(hour_difference, 24);
//                                time_ago2 = day_difference + " days ago";
//                            }
//                            else {
//                                time_ago2 = hour_difference + " hours ago";
//                            }
//                        }
//                        else {
//                            time_ago_view.setVisibility(View.GONE);
//                        }
//
//                        time_view.setText(data.getStringExtra("Time") + "  " + data.getStringExtra("Date") + "  " + data.getStringExtra("Location"));
//
//                        Glide.with(this)
//                    .load(mood_image_fileurl)
//                    .into(mood_image_view);
//
//                        Glide.with(this)
//                    .load(profile_image_fileurl)
//                    .into(profile_picture_view);
//
//                    }
//                });


    }

}
