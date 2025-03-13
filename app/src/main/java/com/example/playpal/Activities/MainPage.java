package com.example.playpal.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.playpal.Models.User;
import com.example.playpal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainPage extends AppCompatActivity {

    private TextView tvGreeting;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
        private Button btnLogin, btnRegister;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main_page);

            // אתחול של הממשק
            tvGreeting = findViewById(R.id.tvGreeting);

            // אתחול של Firebase
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference("Users");

            // בדוק אם יש משתמש מחובר
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // אם יש משתמש מחובר, שלוף את השם
                String userId = currentUser.getUid();
                mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // קבלת נתוני המשתמש מהמאגר
                        User user = dataSnapshot.getValue(User.class);

                        if (user != null) {
                            // הצגת שם המשתמש ב-TextView
                            tvGreeting.setText("שלום " + user.getUsername());  // משתנה המייצג את שם המשתמש
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("MainPage", "Failed to read user data", databaseError.toException());
                    }
                });
            } else {
                // אם אין משתמש מחובר, הצג הודעה מתאימה
                tvGreeting.setText("שלום, אנא התחבר!");
            }
        }
    }