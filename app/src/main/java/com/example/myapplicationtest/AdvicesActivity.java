package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdvicesActivity extends AppCompatActivity {
    private TextView adviceTextView;
    private ImageButton backBtn;
    private Button okButton;
    private TextToSpeech tts;
    private SharedPreferences sharedPreferences;
    private ImageView volumeIcon;
    private static final String PREFS_NAME = "AdvicesPrefs";
    private static final String KEY_ADVICE = "advice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_advices);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        adviceTextView = findViewById(R.id.adviceTextView);
        backBtn = findViewById(R.id.back);
        okButton = findViewById(R.id.okButton);
        volumeIcon = findViewById(R.id.volumeIcon);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                tts.setPitch(0.00000000001f);
                tts.setSpeechRate(1.6f);
                Set<Voice> voices = tts.getVoices();
                List<Voice> voiceList = new ArrayList<>(voices);
                Voice selectedVoice = voiceList.get(11);
                tts.setVoice(selectedVoice);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("tts", "Language not supported");
                }
            } else {
                Log.e("tts", "Initialization failed");
            }
        });

        String advice = getIntent().getStringExtra("advice");
        if (advice != null) {
            saveAdvice(advice);
        }

        String savedAdvice = getSavedAdvice();
        if (savedAdvice != null) {
            adviceTextView.setText(savedAdvice);
        } else {
            adviceTextView.setText("Submit your mood to get advice");
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdvicesActivity.this, DataActivity.class);
                startActivity(intent);
                finish();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdvicesActivity.this, DataActivity.class);
                startActivity(intent);
                finish();
            }
        });
        volumeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak(savedAdvice, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }

    private void saveAdvice(String advice) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ADVICE, advice);
        editor.apply();
    }

    private String getSavedAdvice() {
        return sharedPreferences.getString(KEY_ADVICE, null);
    }
}
