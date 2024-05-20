package com.example.myapplicationtest;

import static com.example.myapplicationtest.MainActivity.getDate;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatListActivity extends AppCompatActivity {

    private DatabaseReference chatsRef;
    private ListView chatListView;
    private ChatAdapter adapter;
    private List<ChatClass> chatList = new ArrayList<>();
    private SearchView searchView;
    private String currentUserID;
    private FirebaseAuth mAuth;

    private int[] imageResIds = {
            R.drawable.whitechar,
            R.drawable.colorfulchar,
            R.drawable.yellowchar,
            R.drawable.appchar,
    };
    int randomIndex = new Random().nextInt(imageResIds.length);
    int randomImageResId = imageResIds[randomIndex];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        searchView = findViewById(R.id.search_bar);
        chatListView = findViewById(R.id.listview);
        adapter = new ChatAdapter(this, chatList);
        chatListView.setAdapter(adapter);

        setupSearchView();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();
        retrieveChatsFromFirebase(currentUserID);



        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatClass chat = adapter.getItem(position);
                String chatName = chat.getChatName();
                int imageResId = chat.getImageResId();
                openChat(chatName, imageResId);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
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

    public void more(View view) {
        Dialog dialog = new Dialog(ChatListActivity.this);
        dialog.setContentView(R.layout.popup_options);
        ImageButton deleteButton = dialog.findViewById(R.id.delete_chat);
    }

    private void filter(String query) {
        List<ChatClass> filteredList = new ArrayList<>();
        for (ChatClass chat : chatList) {
            if (chat.getChatName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(chat);
            }
        }
        adapter = new ChatAdapter(this, filteredList);
        chatListView.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
                searchView.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
    }

    private void retrieveChatsFromFirebase(String userID) {
        chatsRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatClass chat = snapshot.getValue(ChatClass.class);
                    if (chat != null) {

                        chatList.add(chat);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }



    public void openChat(String chatName, int imageResId) {
        if (chatName != null && !chatName.isEmpty()) {
            Intent intent = new Intent(ChatListActivity.this, MainActivity.class);
            intent.putExtra("chatName", chatName);
            intent.putExtra("imageResId", imageResId);
            startActivity(intent);
        } else {

        }
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
        int randomIndex = new Random().nextInt(imageResIds.length);
        int selectedImageResId = imageResIds[randomIndex];

        ChatClass newChat = new ChatClass(date, chatName, selectedImageResId);

        chatsRef.child(currentUserID).child(chatName).setValue(newChat)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ChatListActivity.this, "New chat created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
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
                callback.onDialogSubmit(userInput);
            }
        });

        dialog.show();
    }
}
