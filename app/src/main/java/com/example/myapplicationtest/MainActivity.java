package com.example.myapplicationtest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private ImageButton sendQueryButton;
    private TextInputEditText queryEditText;
    private ProgressBar progressBar;
    private ChatFutures chatModel;
    List<MessageClass> messageList;
    MessageAdapter messageAdapter;
    RecyclerView recyclerView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_first_gemini_app);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        messageList = new ArrayList<>();
        chatModel = getChatModel();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sendQueryButton = findViewById(R.id.sendPromptButton);
        progressBar = findViewById(R.id.sendPromptProgressBar);
        queryEditText = findViewById(R.id.queryEditText);
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

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
                } else {
                    sendQueryButton.setEnabled(true);
                }
            } else {
                Log.e("tts", "Initialization failed");
            }
        });

        sendQueryButton.setOnClickListener(v -> {
            String query = queryEditText.getText().toString() + "You are my best friend, your name it Mateo, care and help me, answer short and answer only messages coming after this sentence" ;
            String showQuery = queryEditText.getText().toString();
            addToChat(query, MessageClass.SENT_BY_ME);
            queryEditText.setText("");

            // Sending user query to the database
            sendMessageToDatabase(showQuery, MessageClass.SENT_BY_ME);

            progressBar.setVisibility(View.VISIBLE); // Show progress bar while waiting for AI response

            GeminiPro.getResponse(chatModel, query, new ResponseCallback() {
                @Override
                public void onResponse(String response) {
                    progressBar.setVisibility(View.GONE);
                    tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, null);
                    addToChat(response, MessageClass.SENT_BY_BOT);
                    sendMessageToDatabase(response, MessageClass.SENT_BY_BOT);
                }

                @Override
                public void onError(Throwable throwable) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Error getting response from AI", Toast.LENGTH_SHORT).show();
                }
            });
        });

        retrieveMessagesFromFirebase();
    }

    private ChatFutures getChatModel() {
        GeminiPro model = new GeminiPro();
        GenerativeModelFutures modelFutures = model.getModel();
        return modelFutures.startChat();
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new MessageClass(message, sentBy));
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    static String getDate() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    public void sendMessageToDatabase(String text, String sentBy) {
        MessageClass message = new MessageClass(text, sentBy);
        String deltaTime = getDate();
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(deltaTime);
        messagesRef.setValue(message);
//                .addOnSuccessListener(aVoid -> Toast.makeText(FirstGeminiApp.this, "Message sent to database", Toast.LENGTH_SHORT).show())
//                .addOnFailureListener(e -> Toast.makeText(FirstGeminiApp.this, "Failed to send message to database", Toast.LENGTH_SHORT).show());
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
                messageList.clear(); // Clear existing messages
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageClass message = snapshot.getValue(MessageClass.class);
                    if (message != null) {
//                        Toast.makeText(FirstGeminiApp.this, "Message", Toast.LENGTH_SHORT).show();
                        addToChat(message.getMessage(), message.getSentBy());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
//                Log.e("Firebase", "Error fetching messages", databaseError.toException());
            }
        });
    }
}