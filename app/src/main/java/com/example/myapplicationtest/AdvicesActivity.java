package com.example.myapplicationtest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

public class AdvicesActivity extends AppCompatActivity {

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference moodRef;
    private BarChart barChart;


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
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();
        moodRef = FirebaseDatabase.getInstance().getReference().child("moods").child(currentUserID);
        barChart = findViewById(R.id.barChart);
        ImageButton backBtn = findViewById(R.id.back);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.mood);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.chat) {
                return true;
            } else if (itemId == R.id.chat) {
                startActivity(new Intent(getApplicationContext(), AdvicesActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.home) {
                startActivity(new Intent(getApplicationContext(), ChatListActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.settings) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.mood) {
                startActivity(new Intent(getApplicationContext(), MoodActivity.class));
                finish();
                return true;
            }
            return false;
        });

        String advice = getIntent().getStringExtra("advice");
        TextView adviceTextView = findViewById(R.id.adviceTextView);
        adviceTextView.setText(advice);
        retrieveMoodsFromDatabase();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdvicesActivity.this, MoodActivity.class);
                startActivity(intent);
            }
        });


    }
    private void retrieveMoodsFromDatabase() {
        moodRef.limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<BarEntry> entries = new ArrayList<>();
                ArrayList<String> dates = new ArrayList<>();
                HashSet<String> uniqueDates = new HashSet<>(); // HashSet to store unique dates

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    if (!uniqueDates.contains(date)) { // Check if the date is already added
                        uniqueDates.add(date); // Add date to HashSet
                        int totalMoodLevel = 0;
                        int moodCount = 0;
                        for (DataSnapshot moodSnapshot : dateSnapshot.getChildren()) {
                            totalMoodLevel += moodSnapshot.child("moodLevel").getValue(Integer.class);
                            moodCount++;
                        }
                        float averageMoodLevel = (float) totalMoodLevel / moodCount;
                        entries.add(new BarEntry(entries.size(), averageMoodLevel));
                        dates.add(date);
                    }
                }
                displayBarChart(entries, dates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdvicesActivity.this, "Failed to retrieve mood data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void displayBarChart(ArrayList<BarEntry> entries, ArrayList<String> dates) {
        BarDataSet dataSet = new BarDataSet(entries, "Mood Levels");
        dataSet.setColors(getMoodColors(entries));
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.invalidate();
        barChart.getAxisRight().setDrawGridLines(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
    }


    private void drawEmojisOnBars(ArrayList<BarEntry> entries) {
        for (int i = 0; i < entries.size(); i++) {
            float x = entries.get(i).getX();
            float y = entries.get(i).getY();
            int emojiResId = getEmojiResId(y);
            drawEmojiOnBar(x, y, emojiResId);
        }
    }

    private void drawEmojiOnBar(float x, float y, int emojiResId) {
        float iconSize = 80f; // Adjust the size as needed
        float posX = x - iconSize / 2;
        float posY = y;
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(emojiResId);
        imageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        imageView.setX(posX);
        imageView.setY(posY);
        imageView.setLayoutParams(new ViewGroup.LayoutParams((int) iconSize, (int) iconSize));
        barChart.addView(imageView);
    }

    private int getEmojiResId(float moodLevel) {
        // Determine emoji based on mood level
        if (moodLevel >= 4) {
            return R.drawable.amazing;
        } else if (moodLevel >= 3) {
            return R.drawable.happy;
        } else if (moodLevel >= 2) {
            return R.drawable.nervous;
        } else if (moodLevel >= 1) {
            return R.drawable.upset;
        } else {
            return R.drawable.sad;
        }
    }

    private int[] getMoodColors(ArrayList<BarEntry> entries) {
        int[] colors = new int[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            float moodLevel = entries.get(i).getY();
            colors[i] = getMoodColor(moodLevel);
        }
        return colors;
    }

    private int getMoodColor(float moodLevel) {
        // Customize colors based on mood level
        if (moodLevel >= 4) {
            return Color.rgb(255, 193, 7); // Yellow
        } else if (moodLevel >= 3) {
            return Color.rgb(76, 175, 80); // Green
        } else if (moodLevel >= 2) {
            return Color.rgb(33, 150, 243); // Blue
        } else if (moodLevel >= 1) {
            return Color.rgb(255, 87, 34); // Orange
        } else {
            return Color.rgb(233, 30, 99); // Pink
        }
    }
}