package com.example.myapplicationtest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private DatabaseReference chatsRef;
    private ListView chatListView;
    private ChatAdapter adapter;
    private List<ChatClass> chatList = new ArrayList<>(); // Store chat objects instead of chat names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        chatListView = findViewById(R.id.listview);

        adapter = new ChatAdapter(this, chatList); // Pass chatList to adapter
        chatListView.setAdapter(adapter);

        // Retrieve existing chats from Firebase
        retrieveChatsFromFirebase();
    }

    private void retrieveChatsFromFirebase() {
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatList.clear(); // Clear existing chat list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatClass chat = snapshot.getValue(ChatClass.class);
                    if (chat != null) {
                        chatList.add(chat);
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter of data change
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    public void openChat(View view) {
        startActivity(new Intent(ChatListActivity.this, MainActivity.class));
    }

    public void onAddChatButtonClick(View view) {
        String chatId = generateUniqueIdForChat();
        createNewChat(chatId, "Chatik Nameik");
    }

    public static String generateUniqueIdForChat() {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        return chatsRef.push().getKey();
    }

    private void createNewChat(String chatId, String chatName) {
        ChatClass newChat = new ChatClass(chatId, chatName);
        chatsRef.child(chatId).setValue(newChat)
                .addOnSuccessListener(aVoid -> {
                    // Chat successfully created in Firebase
                    Toast.makeText(ChatListActivity.this, "New chat created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to create chat in Firebase
                    Toast.makeText(ChatListActivity.this, "Failed to create new chat", Toast.LENGTH_SHORT).show();
                });
    }
}
