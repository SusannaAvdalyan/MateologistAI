package com.example.myapplicationtest;

public class MoodClass {
    String mood;
    String moodText;

    public MoodClass(){
        // Default constructor required for Firebase
    }
    public String getMoodText() {
        return moodText;
    }
    public String setMoodText() {
        return moodText;
    }
    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public MoodClass(String mood, String moodText) {
        this.mood = mood;
        this.moodText = moodText;
    }
}
