package com.example.fitcheck;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcheck.Aktiviteler;
import com.example.fitcheck.Bmi;
import com.example.fitcheck.KaloriGiris;
import com.example.fitcheck.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button btnCalorieCalculator, btnBMI, btnActivities;
    private TextView txtTotalCalories;
    private int totalCalories = 0;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        btnCalorieCalculator = findViewById(R.id.btnCalorieCalculator);
        btnBMI = findViewById(R.id.btnBMI);
        btnActivities = findViewById(R.id.btnActivities);
        txtTotalCalories = findViewById(R.id.txtTotalCalories);

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("records").child(userId);
        myRef.child("calori").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("StringFormatMatches")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateTotalCalories();
                } else {
                    Toast.makeText(MainActivity.this, "hata", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Intent calorieIntent = new Intent(MainActivity.this, KaloriGiris.class);
        Intent bmiIntent = new Intent(MainActivity.this, Bmi.class);
        Intent activitiesIntent = new Intent(MainActivity.this, Aktiviteler.class);

        btnCalorieCalculator.setOnClickListener(v -> startActivity(calorieIntent));
        btnBMI.setOnClickListener(v -> startActivity(bmiIntent));
        btnActivities.setOnClickListener(v -> startActivity(activitiesIntent));
    }



    public void updateTotalCalories() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("records").child(userId);
        myRef.child("calori").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("StringFormatMatches")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String changeCalori = snapshot.getValue(String.class);
                    if (changeCalori != null) {
                        txtTotalCalories.setText(changeCalori);
                        Log.d("MainActivity", "Calorie value retrieved: " + changeCalori);
                    } else {
                        Log.e("MainActivity", "Calorie value is null");
                        Toast.makeText(MainActivity.this, "Calorie value is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("MainActivity", "Snapshot does not exist at path: records/" + userId + "/calori");
                    Toast.makeText(MainActivity.this, "hata", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Database error: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public int getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(int totalCalories) {
        this.totalCalories = totalCalories;
    }
}
