package com.example.myapplicationtest;

public interface ResponseCallback {
    void onResponse(String response);
    void onError(Throwable throwable);
}