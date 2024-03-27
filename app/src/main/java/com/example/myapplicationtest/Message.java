package com.example.myapplicationtest;

public class Message {
    private String text_user;
    private String text_ai;

    public Message() {
        // Default constructor required for Firebase
    }

    public Message(String text_user, String text_ai) {
        this.text_user = text_user;
        this.text_ai = text_ai;
    }

    public String getText_user() {
        return text_user;
    }

    public void setText_user(String text_user) {
        this.text_user = text_user;
    }

    public String getText_ai() {
        return text_ai;
    }

    public void setText_ai(String text_ai) {
        this.text_ai = text_ai;
    }
}


