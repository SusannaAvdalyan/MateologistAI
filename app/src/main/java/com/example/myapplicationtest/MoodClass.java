package com.example.myapplicationtest;

public class MoodClass {
    String mood;

    public MoodClass(){
        // Default constructor required for Firebase
    }
    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public MoodClass(String mood) {
        this.mood = mood;

    }
}
