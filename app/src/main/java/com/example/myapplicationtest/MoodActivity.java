package com.example.myapplicationtest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MoodActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private SeekBar seekBar;
    private Button submitButton;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private EditText moodText;
    private ProgressBar progressBar;
    private DatabaseReference moodRef;
    private BarChart barChart;
    private ChatFutures chatModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        moodText = findViewById(R.id.moodTextInput);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textViewFeeling);
        seekBar = findViewById(R.id.seekBar);
        submitButton = findViewById(R.id.submitButton);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        chatModel = getChatModel();
        currentUserID = currentUser.getUid();
        moodRef = FirebaseDatabase.getInstance().getReference().child("moods").child(currentUserID);
        progressBar = findViewById(R.id.sendPromptProgressBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateImageAndText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = seekBar.getProgress();
                String query = "Hey, Could you please provide suggestions based on the mood I'm expressing in my texts? Just send your suggestions and nothing more, too long, write just a few essential points. Thanks!" + moodText.getText().toString();
                String mood = textView.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                GeminiPro.getResponse(chatModel, query, new ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        sendMoodToDatabase(progress, moodText.getText().toString(), response);
                        Intent intent = new Intent(MoodActivity.this, AdvicesActivity.class);
                        intent.putExtra("advice", response);
                        startActivity(intent);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MoodActivity.this, "Error getting response from AI", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.mood);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.mood) {
                return true;
            } else if (itemId == R.id.chat) {
                startActivity(new Intent(getApplicationContext(), DataActivity.class));
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
            } else if (itemId == R.id.mood) {
                startActivity(new Intent(getApplicationContext(), MoodActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private ChatFutures getChatModel() {
        GeminiPro model = new GeminiPro();
        GenerativeModelFutures modelFutures = model.getModel();
        return modelFutures.startChat();
    }

    private void updateImageAndText(int progress) {
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

    private void sendMoodToDatabase(int moodLevel, String moodText, String advice) {
        String dataTime = getCurrentDateTime();
        DatabaseReference moodEntryRef = moodRef.child(dataTime).push();
        moodEntryRef.child("moodLevel").setValue(moodLevel);
        moodEntryRef.child("moodText").setValue(moodText);
        moodEntryRef.child("moodAdvice").setValue(advice);
    }


    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}