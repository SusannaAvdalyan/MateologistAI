package com.example.myapplicationtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerify extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verify);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Check if user is not null and email is not verified
        if (user != null && !user.isEmailVerified()) {
            // Trigger email verification
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EmailVerify.this, R.string.if_open_email, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If user is null or email is already verified, proceed to MainActivity
            startActivity(new Intent(EmailVerify.this, MainActivity.class));
            finish();
        }
    }
}
