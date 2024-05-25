package com.example.myapplicationtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import java.util.Random;

public class ChatAdapter extends ArrayAdapter<ChatClass> {

    private Context mContext;
    private List<ChatClass> mChatList;
    private String currentUserID;
    private FirebaseAuth mAuth;

    private int[] imageResIds = {
            R.drawable.whitechar,
            R.drawable.colorfulchar,
            R.drawable.yellowchar,
            R.drawable.appchar,
    };

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

        ChatClass currentChat = mChatList.get(position);

        TextView chatNameTextView = listItem.findViewById(R.id.listName);
        chatNameTextView.setText(currentChat.getChatName());

        ImageView listImage = listItem.findViewById(R.id.listImage);
        if (currentChat.getImageResId() == 0) {
            int randomIndex = new Random().nextInt(imageResIds.length);
            int selectedImageResId = imageResIds[randomIndex];
            currentChat.setImageResId(selectedImageResId);
        }
        listImage.setImageResource(currentChat.getImageResId());

        ImageView btnMoreOptions = listItem.findViewById(R.id.moreBtn);
        btnMoreOptions.setOnClickListener(v -> {
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
        popupWindow.showAsDropDown(anchorView);
    }
    private void deleteChat(ChatClass chat) {
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
