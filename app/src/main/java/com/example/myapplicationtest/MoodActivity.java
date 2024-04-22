package com.example.myapplicationtest;
import static com.example.myapplicationtest.MainActivity.getDate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MoodActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private SeekBar seekBar;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        seekBar = findViewById(R.id.seekBar);
        submitButton = findViewById(R.id.submitButton);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.mood);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.mood) {
                return true;
            } else if (itemId == R.id.chat) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.home) {
                startActivity(new Intent(getApplicationContext(), ChatListActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.settings) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                finish();
                return true;
            }
            return false;
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Change image and text based on seek bar progress
                updateImageAndText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mood = textView.getText().toString();
                String date = getCurrentDateTime();
                sendMoodToDatabase(mood, date);
                startActivity(new Intent(MoodActivity.this, MainActivity.class));
            }
        });

    }

    private void updateImageAndText(int progress) {
        // Update image based on progress
        switch (progress) {
            case 0:
                imageView.setImageResource(R.drawable.sad);
                textView.setText("Sad");
                break;
            case 1:
                imageView.setImageResource(R.drawable.upset);
                textView.setText("Upset");
                break;
            case 2:
                imageView.setImageResource(R.drawable.nervous);
                textView.setText("Nervous");
                break;
            case 3:
                imageView.setImageResource(R.drawable.happy);
                textView.setText("Happy");
                break;
            case 4:
                imageView.setImageResource(R.drawable.amazing);
                textView.setText("Amazing");
                break;
        }
    }

    public void sendMoodToDatabase(String mood, String date) {
        MoodClass moodData = new MoodClass(mood);
        String deltaTime = getDate();
        DatabaseReference moodRef = FirebaseDatabase.getInstance().getReference("moods").child(deltaTime);
        moodRef.setValue(moodData);
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
