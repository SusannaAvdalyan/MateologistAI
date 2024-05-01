package com.example.myapplicationtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ChatAdapter extends ArrayAdapter<ChatClass> {

    private Context mContext;
    private List<ChatClass> mChatList;
    private String currentUserID;
    private FirebaseAuth mAuth;

    public ChatAdapter(Context context, List<ChatClass> chatList) {

        super(context, 0, chatList);
        mContext = context;
        mChatList = chatList;
    }

    @SuppressLint("WrongViewCast")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        }

        // Get the current chat object
        ChatClass currentChat = mChatList.get(position);

        // Set the chat name
        TextView chatNameTextView = listItem.findViewById(R.id.listName);
        chatNameTextView.setText(currentChat.getChatName());

        ImageView btnMoreOptions = listItem.findViewById(R.id.moreBtn);
        btnMoreOptions.setOnClickListener(v -> {
            // Show options popup window when more options button is clicked
            showOptionsPopup(v, currentChat);
        });

        return listItem;
    }

    private void showOptionsPopup(View anchorView, ChatClass chat) {
        View popupView = LayoutInflater.from(mContext).inflate(R.layout.popup_options, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        TextView txtDelete = popupView.findViewById(R.id.txtDelete);
        txtDelete.setOnClickListener(v -> {
            deleteChat(chat);
            popupWindow.dismiss();
        });

        // Show the popup window next to the anchor view
        popupWindow.showAsDropDown(anchorView);
    }

    // Method to delete chat
    private void deleteChat(ChatClass chat) {
        MessageClass message = new MessageClass();
        mChatList.remove(chat);
        notifyDataSetChanged();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats")
                .child(currentUserID)
                .child(chat.getChatName());
        chatRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(mContext, "Chat deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(mContext, "Failed to delete chat", Toast.LENGTH_SHORT).show();
                });
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages")
                .child(currentUserID)
                .child(chat.getChatName());
        messagesRef.removeValue();

    }
}