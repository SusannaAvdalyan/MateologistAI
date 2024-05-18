package com.example.myapplicationtest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
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

public class DataActivity extends AppCompatActivity {

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference moodRef;
    private BarChart barChart;
    private CardView cardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data);
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
        cardView = findViewById(R.id.card_view);
        ImageButton backBtn = findViewById(R.id.back);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.mood);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.chat) {
                return true;
            } else if (itemId == R.id.chat) {
                startActivity(new Intent(getApplicationContext(), DataActivity.class));
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


        retrieveMoodsFromDatabase();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DataActivity.this, MoodActivity.class);
                startActivity(intent);
            }
        });
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DataActivity.this, AdvicesActivity.class);
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
                HashSet<String> uniqueDates = new HashSet<>();

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    if (!uniqueDates.contains(date)) {
                        uniqueDates.add(date);
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
                Toast.makeText(DataActivity.this, "Failed to retrieve mood data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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


        barChart.setDrawValueAboveBar(false);

        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return getMoodName(value);
            }
        });
        barData.setValueTextSize(12f);
        barData.setValueTextColor(Color.WHITE);
        barChart.invalidate();
    }



    private String getMoodName(float moodLevel) {
        if (moodLevel >= 4) {
            return "Amazing 😊";
        } else if (moodLevel >= 3) {
            return "Happy 😄";
        } else if (moodLevel >= 2) {
            return "Nervous 😬";
        } else if (moodLevel >= 1) {
            return "Upset 😔";
        } else {
            return "Sad 😞";
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
        if (moodLevel >= 4) {
            return Color.rgb(241, 230, 75); // Yellow
        } else if (moodLevel >= 3) {
            return Color.rgb(0, 204, 102); // Green
        } else if (moodLevel >= 2) {
            return Color.rgb(101, 175, 255); // Blue
        } else if (moodLevel >= 1) {
            return Color.rgb(255, 173, 28); // Orange
        } else {
            return Color.rgb(233, 30, 99); // Pink
        }
    }

}