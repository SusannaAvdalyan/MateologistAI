package com.example.myapplicationtest;

public class ChatClass {
    private String date;
    private String chatName;
    private int imageResId;

    // No-argument constructor
    public ChatClass() {
        // Default constructor required for calls to DataSnapshot.getValue(ChatClass.class)
    }

    // Constructor with all fields
    public ChatClass(String date, String chatName, int imageResId) {
        this.date = date;
        this.chatName = chatName;
        this.imageResId = imageResId;
    }

    // Constructor without imageResId
    public ChatClass(String date, String chatName) {
        this.date = date;
        this.chatName = chatName;
    }

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}
