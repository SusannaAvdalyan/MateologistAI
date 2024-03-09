package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.android.material.textfield.TextInputEditText;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class FirstGeminiApp extends AppCompatActivity {

    private TextInputEditText queryEditText;
    private Button sendQueryButton;
    private ProgressBar progressBar;
    private LinearLayout chatBodyContainer;
    private ChatFutures chatModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first_gemini_app);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        chatModel = getChatModel();

        queryEditText = findViewById(R.id.queryEditText);
        sendQueryButton = findViewById(R.id.sendPromptButton);
        progressBar = findViewById(R.id.sendPromptProgressBar);
        chatBodyContainer = findViewById(R.id.chatResponseLayout);



        sendQueryButton.setOnClickListener(v -> {
            String query = queryEditText.getText().toString() + "Your name is Mateo and you are my soulmate. Help and support me please. Answer short and unformal dont write this everytime";
            String showQuery = queryEditText.getText().toString();
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(this, queryEditText.getText().toString(), Toast.LENGTH_SHORT).show();
            queryEditText.setText("");
            populateChatBody("You", showQuery, getDate());


            GeminiPro.getResponse(chatModel, query, new ResponseCallback() {
                @Override
                public void onResponse(String response) {
                    progressBar.setVisibility(View.GONE);
                    populateChatBody("Mateo", response, getDate());
                }

                @Override
                public void onError(Throwable throwable) {
                    populateChatBody("Mateo", "Sorry, I'm having trouble understanding that. Please try again.", getDate());
                    progressBar.setVisibility(View.GONE);
                }
            });
        });
    }

    private ChatFutures getChatModel() {
        GeminiPro model = new GeminiPro();
        GenerativeModelFutures modelFutures = model.getModel();

        return modelFutures.startChat();
    }

    public void populateChatBody(String userName, String message, String date) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_message_block, null);

        TextView userAgentName = view.findViewById(R.id.userAgentNameTextView);
        TextView userAgentMessage = view.findViewById(R.id.userAgentMessageTextView);
        TextView dateTextView = view.findViewById(R.id.dateTextView);

        userAgentName.setText(userName);
        userAgentMessage.setText(message);
        dateTextView.setText(date);

        chatBodyContainer.addView(view);

        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private String getDate() {
        Instant instant = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm").withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }
    public void getSpeechInput(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    queryEditText.setText(result.get(0));

                }
                break;
        }
    }


}