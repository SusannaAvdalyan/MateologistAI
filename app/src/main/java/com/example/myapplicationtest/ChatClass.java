package com.example.myapplicationtest;

public class ChatClass {
    private String date;
    private String chatName;

    // Default constructor (required for Firebase)
    public ChatClass() {
    }

    // Constructor
    public ChatClass(String date, String chatName) {
        this.date = date;
        this.chatName = chatName;
    }

    // Getters and setters
    public String getChatId() {
        return date;
    }

    public void setChatId(String chatId) {
        this.date = date;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

}

