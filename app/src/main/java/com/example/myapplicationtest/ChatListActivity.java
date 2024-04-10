package com.example.myapplicationtest;

import static com.example.myapplicationtest.MainActivity.getDate;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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

        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the chat object from the adapter
                ChatClass chat = adapter.getItem(position);

                // Check if the chat object is not null
                if (chat != null) {
                    // Get the chat name
                    String chatName = chat.getChatName();

                    // Open the chat in MainActivity
                    openChat(chatName);
                }
            }
        });

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


    public void openChat(String chatName) {
        Intent intent = new Intent(ChatListActivity.this, MainActivity.class);
        intent.putExtra("chatName", chatName); // Pass the chat name to MainActivity
        startActivity(intent);
    }


    public void onAddChatButtonClick(View view) {
        openDialog(new DialogCallback() {
            @Override
            public void onDialogSubmit(String userInput) {
                String date = getDate();
                createNewChat(date, userInput);
            }
        });
    }

    public static String generateUniqueIdForChat() {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        return chatsRef.push().getKey();
    }

    private void createNewChat(String date, String chatName) {
        ChatClass newChat = new ChatClass(date, chatName);
        chatsRef.child(chatName).setValue(newChat)
                .addOnSuccessListener(aVoid -> {
                    // Chat successfully created in Firebase
                    Toast.makeText(ChatListActivity.this, "New chat created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to create chat in Firebase
                    Toast.makeText(ChatListActivity.this, "Failed to create new chat", Toast.LENGTH_SHORT).show();
                });
    }

    private interface DialogCallback {
        void onDialogSubmit(String userInput);
    }

    private void openDialog(final DialogCallback callback) {
        final Dialog dialog = new Dialog(ChatListActivity.this);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.setCancelable(true);

        final EditText editText = dialog.findViewById(R.id.editText);
        Button submitButton = dialog.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = editText.getText().toString();
                Toast.makeText(ChatListActivity.this, "Submitted: " + userInput, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                callback.onDialogSubmit(userInput); // Callback with user input
            }
        });

        dialog.show();
    }

}
