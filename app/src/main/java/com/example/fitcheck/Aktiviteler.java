package com.example.fitcheck;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Aktiviteler extends AppCompatActivity {

    private ListView listViewActivities;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> activityList;
    private DatabaseReference activitiesRef;
    private DatabaseReference caloriesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aktiviteler);

        activitiesRef = FirebaseDatabase.getInstance().getReference().child("activities");
        caloriesRef = FirebaseDatabase.getInstance().getReference().child("calories");

        listViewActivities = findViewById(R.id.listView_activities);
        activityList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, activityList);
        listViewActivities.setAdapter(adapter);

        fetchActivitiesFromFirebase();

        listViewActivities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedFood = activityList.get(position);
                DatabaseReference activitiesRef = caloriesRef.child(selectedFood);
                activitiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String calories = dataSnapshot.getValue(String.class);
                            Toast.makeText(Aktiviteler.this, "Kalori Verildi.", Toast.LENGTH_SHORT).show();
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            addCaloriesToDatabase(userId, calories);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Aktiviteler.this, "Kalori alınırken bir hata oluştu", Toast.LENGTH_SHORT).show();
                        Log.d("KaloriGiris", "kontrol:0");
                    }
                });
            }
        });

    }

    private void fetchActivitiesFromFirebase() {
        activitiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot activitySnapshot : dataSnapshot.getChildren()) {
                    String activityName = activitySnapshot.getKey();
                    activityList.add(activityName);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Aktiviteler.this, "Aktiviteler alınırken bir hata oluştu", Toast.LENGTH_SHORT).show();
                Log.d("FoodsAdapter", "kontrol:0");
            }
        });
    }

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
        Intent intent = new Intent(Aktiviteler.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}
