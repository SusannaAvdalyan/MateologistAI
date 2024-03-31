package com.example.myapplicationtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FirstGeminiApp extends AppCompatActivity {

    private TextToSpeech tts;
    private TextInputEditText queryEditText;
    private ImageButton sendQueryButton;
    private ProgressBar progressBar;
    private LinearLayout chatBodyContainer;
    private ChatFutures chatModel;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        retrieveMessagesFromFirebase();
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



        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = tts.setLanguage(Locale.US);
                    tts.setPitch(0.00000000001f);
                    tts.setSpeechRate(1.6f);
                    Set<Voice> voices = tts.getVoices();
                    List<Voice> voiceList = new ArrayList<>(voices);
                    Voice selectedVoice = voiceList.get(11);
                    tts.setVoice(selectedVoice);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("tts", "Language not supported");
                    } else {
                        sendQueryButton.setEnabled(true);
                    }
                } else {
                    Log.e("tts", "Initialization failed");
                }
            }
        });



        sendQueryButton.setOnClickListener(v -> {
            String query = queryEditText.getText().toString() + "Act like your name is Mateo and you are my soulmate, answer short and unformal" ;
            String showQuery = queryEditText.getText().toString();
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(this, queryEditText.getText().toString(), Toast.LENGTH_SHORT).show();
            queryEditText.setText("");
            populateChatBody("You", showQuery, getDate());


            GeminiPro.getResponse(chatModel, query, new ResponseCallback() {
                @Override
                public void onResponse(String response) {
                    progressBar.setVisibility(View.GONE);
                    tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, null);
                    populateChatBody("Mateo", response, getDate());
                    sendMessageToDatabase(showQuery, response);
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
        View view;

        if (userName.equals("You")) {
            view = inflater.inflate(R.layout.item_user_message, null);
        } else {
            view = inflater.inflate(R.layout.item_ai_message, null);
        }

        TextView messageTextView = view.findViewById(R.id.messageTextView);
        messageTextView.setText(message);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        if (userName.equals("You")) {
            layoutParams.gravity = Gravity.END;
        } else {
            layoutParams.gravity = Gravity.START;
        }

        view.setLayoutParams(layoutParams);

        chatBodyContainer.addView(view);

        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }



    private static String getDate() {
        long currentTimeMillis = System.currentTimeMillis();
        String currentTimeString = String.valueOf(currentTimeMillis);

        return currentTimeString;
    }
    public void getSpeechInput(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Doesn't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }


    public void sendMessageToDatabase(String userText, String aiText) {
        Message message = new Message(userText, aiText);
        String deltaTime = getDate();
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(deltaTime);
        Toast.makeText(this, "Function", Toast.LENGTH_SHORT).show();
        messagesRef.setValue(message);
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

    private void retrieveMessagesFromFirebase() {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear existing chat UI
                chatBodyContainer.removeAllViews();

                // Iterate through messages
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        // Populate chat UI with messages
                        populateChatBody("You", message.getText_user(), getDate());
                        populateChatBody("Mateo", message.getText_ai(), getDate());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                Log.e("Firebase", "Error fetching messages", databaseError.toException());
            }
        });
    }


}