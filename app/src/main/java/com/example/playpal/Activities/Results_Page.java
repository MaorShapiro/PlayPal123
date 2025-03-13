package com.example.playpal.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.playpal.Models.User;
import com.example.playpal.Adapters.UserRawAdapter;
import com.example.playpal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;


public class Results_Page extends AppCompatActivity {

    ListView lvSearch1;
    ArrayList<User> filteredUsers = new ArrayList<>();
    UserRawAdapter userRawAdapter;
    Handler handler = new Handler(Looper.getMainLooper());

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUserId = currentUser.getUid();

    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users");

    ValueEventListener userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_page);

        String game = getIntent().getStringExtra("selectedGame");
        String level = getIntent().getStringExtra("selectedLevel");
        String country = getIntent().getStringExtra("selectedCountry");
        String language = getIntent().getStringExtra("selectedLanguage");

        lvSearch1 = findViewById(R.id.lvSearch1);

        Runnable fetchResults = new Runnable() {
            @Override
            public void run() {
                userListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot currentUserSnapshot) {
                        User currentUserData = currentUserSnapshot.getValue(User.class);

                        List<String> currentUserFriends = (currentUserData != null && currentUserData.getFriends() != null)
                                ? currentUserData.getFriends()
                                : new ArrayList<>();

                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                filteredUsers.clear();

                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    User user = postSnapshot.getValue(User.class);
                                    if (user != null) {
                                        boolean matchesCriteria = (game == null || user.getCurrentGame().equals(game)) &&
                                                (level == null || user.getGetCurrentLevel().equals(level)) &&
                                                (country == null || user.getCountry().equals(country)) &&
                                                (language == null || user.getLanguage().equals(language)) &&
                                                (user.getFriends() == null || !user.getFriends().contains(currentUserId)) &&
                                                (currentUserFriends == null || !currentUserFriends.contains(user.getId())) &&
                                                (user.getId() != null && !user.getId().equals(currentUserId));

                                        if (matchesCriteria) {
                                            filteredUsers.add(user);
                                        }
                                    }
                                }

                                userRawAdapter = new UserRawAdapter(Results_Page.this, filteredUsers);
                                lvSearch1.setAdapter(userRawAdapter);

                                lvSearch1.setOnItemClickListener((parent, view, position, id) -> {
                                    if (filteredUsers.size() > position) {
                                        User selectedUser = filteredUsers.get(position);
                                        Intent intent = new Intent(Results_Page.this, UserPage.class);
                                        intent.putExtra("userId", selectedUser.getId());
                                        startActivity(intent);
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.w("TAG", "Failed to read value.", error.toException());
                                runOnUiThread(() -> Toast.makeText(Results_Page.this, "Error fetching data.", Toast.LENGTH_SHORT).show());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", "Failed to load current user data", databaseError.toException());
                    }
                };
                userRef.addValueEventListener(userListener);

                // קריאה מחדש כל 30 שניות
                handler.postDelayed(this, 30000);
                Toast.makeText(Results_Page.this, "data updated", Toast.LENGTH_SHORT).show();
            }
        };

        // התחלת הרענון האוטומטי
        fetchResults.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userRef.removeEventListener(userListener);
        }
        handler.removeCallbacksAndMessages(null); // מניעת קריאות נוספות
    }
}