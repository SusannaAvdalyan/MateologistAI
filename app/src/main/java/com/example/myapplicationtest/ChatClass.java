package com.example.myapplicationtest;

public class ChatClass {
    private String chatId;
    private String chatName;

    // Default constructor (required for Firebase)
    public ChatClass() {
    }

    // Constructor
    public ChatClass(String chatId, String chatName) {
        this.chatId = chatId;
        this.chatName = chatName;
    }

    // Getters and setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

}

