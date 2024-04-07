package com.example.myapplicationtest;

public class MessageClass {
    public static String SENT_BY_ME = "me";
    public static String SENT_BY_BOT="bot";

    String message;
    String sentBy;
    public MessageClass() {
        // Default constructor required for Firebase
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public MessageClass(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }
}


