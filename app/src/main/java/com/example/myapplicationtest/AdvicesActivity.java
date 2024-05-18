package com.example.myapplicationtest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdvicesActivity extends AppCompatActivity {
    private TextView adviceTextView;
    private ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_advices);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        adviceTextView = findViewById(R.id.adviceTextView);
        backBtn = findViewById(R.id.back);
        String advice = getIntent().getStringExtra("advice");
        if (advice != null) {
            adviceTextView.setText(advice);
        } else {
            adviceTextView.setText("Submit your mood to get advice");
        }
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdvicesActivity.this, DataActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}