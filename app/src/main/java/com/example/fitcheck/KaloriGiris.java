package com.example.fitcheck;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class KaloriGiris extends AppCompatActivity {

    private Spinner spinnerMeals;
    private ListView listViewFoods;
    private ArrayAdapter<String> foodsAdapter;
    private List<String> foodList;

    private DatabaseReference foodsRef;
    private DatabaseReference caloriesRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kalori_giris);

        foodsRef = FirebaseDatabase.getInstance().getReference().child("foods");
        caloriesRef = FirebaseDatabase.getInstance().getReference().child("calories");

        spinnerMeals = findViewById(R.id.spinnerMeals);
        listViewFoods = findViewById(R.id.listViewFoods);

        foodList = new ArrayList<>();

        foodsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, foodList);
        listViewFoods.setAdapter(foodsAdapter);

        fetchMealsFromFirebase();

        spinnerMeals.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMeal = parent.getItemAtPosition(position).toString();
                loadFoodsForMeal(selectedMeal);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        listViewFoods.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedFood = foodList.get(position);
                DatabaseReference foodCaloriesRef = caloriesRef.child(selectedFood);
                foodCaloriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String calories = dataSnapshot.getValue(String.class);
                            Toast.makeText(KaloriGiris.this, "Kalori Alındı", Toast.LENGTH_SHORT).show();
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            addCaloriesToDatabase(userId, calories);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(KaloriGiris.this, "Kalori alınırken bir hata oluştu", Toast.LENGTH_SHORT).show();
                        Log.d("KaloriGiris", "kontrol:0");
                    }
                });
            }
        });
    }

    private void fetchMealsFromFirebase() {
        List<String> mealOptions = new ArrayList<>();
        mealOptions.add("Kahvaltı");
        mealOptions.add("Öğle Yemeği");
        mealOptions.add("Akşam Yemeği");


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mealOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeals.setAdapter(spinnerAdapter);
    }

    private void loadFoodsForMeal(String mealName) {
        DatabaseReference mealRef = foodsRef.child(mealName);
        mealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                foodList.clear();

                for (DataSnapshot foodSnapshot : dataSnapshot.getChildren()) {
                    String foodName = foodSnapshot.getKey();
                    foodList.add(foodName);
                }
                foodsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(KaloriGiris.this, "Yemekler alınırken bir hata oluştu", Toast.LENGTH_SHORT).show();
                Log.d("KJla", "kontrol:0");
            }
        });
    }

    // Make the method public and accept userId parameter
    public void addCaloriesToDatabase(String userId, String calories) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("records").child(userId);
        myRef.child("calori").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String changeCalori = snapshot.getValue(String.class);
                    int curentCalori= Integer.parseInt(changeCalori);
                    int calori = Integer.parseInt(calories);
                    int newChangedCalories = curentCalori + calori;
                    myRef.child("calori").setValue(String.valueOf(newChangedCalories));
                } else {
                    myRef.child("calori").setValue(String.valueOf(calories));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(KaloriGiris.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
