package com.example.myapplicationtest;

public class ChatClass {
    private String date;
    private String chatName;
    private int imageResId;

    public ChatClass() {

    }

    public ChatClass(String date, String chatName, int imageResId) {
        this.date = date;
        this.chatName = chatName;
        this.imageResId = imageResId;
    }

    public ChatClass(String date, String chatName) {
        this.date = date;
        this.chatName = chatName;
    }

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
