package com.example.myapplicationtest;

import static com.example.myapplicationtest.MainActivity.getDate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.ai.client.generativeai.Chat;

import java.util.List;

public class ChatAdapter extends ArrayAdapter<ChatClass> {

    private Context mContext;
    private List<ChatClass> mChatList;

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
        return listItem;
    }
}


