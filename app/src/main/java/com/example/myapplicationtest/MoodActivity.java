package com.example.myapplicationtest;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MoodActivity extends AppCompatActivity {

    private TextView gradeView;
    private TextView textViewFeeling;
    private SeekBar seekBar;
    private Button submitButton, okButton;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private EditText moodTextInput;
    private ProgressBar sendPromptProgressBar;
    private DatabaseReference moodRef;
    private ChatFutures chatModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        moodTextInput = findViewById(R.id.moodTextInput);
        gradeView = findViewById(R.id.gradeView);
        textViewFeeling = findViewById(R.id.textViewFeeling);
        seekBar = findViewById(R.id.seekBar);
        submitButton = findViewById(R.id.submitButton);
        sendPromptProgressBar = findViewById(R.id.sendPromptProgressBar);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        chatModel = getChatModel();
        currentUserID = currentUser != null ? currentUser.getUid() : null;
        moodRef = FirebaseDatabase.getInstance().getReference().child("moods").child(currentUserID);


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
                submitMood();
            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.mood);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.mood) {
                return true;
            } else if (itemId == R.id.home) {
                startActivity(new Intent(getApplicationContext(), ChatListActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.chat) {
                startActivity(new Intent(getApplicationContext(), DataActivity.class));
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
                gradeView.setText("1");
                textViewFeeling.setText("1");
                break;
            case 1:
                gradeView.setText("2");
                textViewFeeling.setText("2");
                break;
            case 2:
                gradeView.setText("3");
                textViewFeeling.setText("3");
                break;
            case 3:
                gradeView.setText("4");
                textViewFeeling.setText("4");
                break;
            case 4:
                gradeView.setText("5");
                textViewFeeling.setText("5");
                break;
            case 5:
                gradeView.setText("6");
                textViewFeeling.setText("6");
                break;
            case 6:
                gradeView.setText("7");
                textViewFeeling.setText("7");
                break;
            case 7:
                gradeView.setText("8");
                textViewFeeling.setText("8");
                break;
            case 8:
                gradeView.setText("9");
                textViewFeeling.setText("9");
                break;
            case 9:
                gradeView.setText("10");
                textViewFeeling.setText("10");
                break;
        }
    }

    private void submitMood() {
        int progress = seekBar.getProgress();
        String moodText = moodTextInput.getText().toString().trim();
        String mood = textViewFeeling.getText().toString();

        if (TextUtils.isEmpty(moodText)) {
            moodTextInput.setError("This field is required");
            return;
        } else {
            moodTextInput.setError(null);
        }

        String query = "Բարև, խնդրում եմ, կարո՞ղ եք առաջարկություններ ներկայացնել՝ ելնելով այն գնահատականից, որը ստացել եմ որոշակի առարկայից, որը ես արտահայտում եմ իմ տեքստերում: Ուղղակի ուղարկեք ձեր առաջարկները և ոչ ավելին, ոչ շատ երկար, գրեք ընդամենը մի քանի կարևոր կետ: Շնորհակալություն" + moodText;
        sendPromptProgressBar.setVisibility(View.VISIBLE);
        showFeedbackDialog();

        GeminiPro.getResponse(chatModel, query, new ResponseCallback() {
            @Override
            public void onResponse(String response) {
                sendMoodToDatabase(progress, moodText, response);
                Intent intent = new Intent(MoodActivity.this, AdvicesActivity.class);
                intent.putExtra("advice", response);
                startActivity(intent);
                sendPromptProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Throwable throwable) {
                sendPromptProgressBar.setVisibility(View.GONE);
                Toast.makeText(MoodActivity.this, "Error getting response from AI", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showFeedbackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mood_submit, null);
        builder.setView(dialogView);
        okButton = dialogView.findViewById(R.id.buttonOK);

        AlertDialog dialog = builder.create();
        dialog.show();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoodActivity.this, AdvicesActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });
    }

    private void sendMoodToDatabase(int moodLevel, String moodText, String advice) {
        String dateTime = getCurrentDateTime();
        DatabaseReference moodEntryRef = moodRef.child(dateTime).push();
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
