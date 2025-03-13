package com.example.playpal.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.playpal.Adapters.ViewPager2Adapter;
import com.example.playpal.Models.User;
import com.example.playpal.R;
import com.example.playpal.Utills.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Random;

public class ViewPaper2Page extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private ViewPager2Adapter customAdapter;
    private ImageView imageView;
    private ImageButton menuButton;
    private ToggleButton allowNotifications;
    private ImageButton sendToFriends;
    private DatabaseReference userRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_paper_2);

        // אתחול רכיבים
        menuButton = findViewById(R.id.menu_button);
        viewPager2 = findViewById(R.id.view_pager2);
        imageView = findViewById(R.id.imageButton3);
        allowNotifications = findViewById(R.id.allowNotifications);
        sendToFriends = findViewById(R.id.sendToFriends);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            loadProfileImage();
            listenForNotifications();
            setupAllowNotifications();
        }

        // מאזינים לכפתורים
        menuButton.setOnClickListener(this::showPopupMenu);
        sendToFriends.setOnClickListener(v -> sendPlayRequest());

        // אתחול ViewPager2 והגדרת כפתורי ניווט
        customAdapter = new ViewPager2Adapter(this);
        viewPager2.setAdapter(customAdapter);
        setupPageNavigation();
    }
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_Login) {
                startActivity(new Intent(this, LoginPage.class));
            } else if (itemId == R.id.action_Register) {
                startActivity(new Intent(this, SignPage.class));
            } else if (itemId == R.id.action_Logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginPage.class));
            }
            return true;
        });

        popup.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // טעינת תמונת המשתמש
    private void loadProfileImage() {
        userRef.child("imageBase64").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String base64Image = dataSnapshot.getValue(String.class);
                if (base64Image != null) {
                    Bitmap bitmap = ImageUtil.convertFrom64base(base64Image);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Profile", "Error loading image", databaseError.toException());
            }
        });
    }

    // שליחת בקשת משחק לכל החברים מהמחלקה `User`
    private void sendPlayRequest() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.getFriends() != null) {
                    List<String> friendsList = user.getFriends();
                    for (String friendId : friendsList) {
                        checkAndSendNotification(friendId);
                    }
                    Toast.makeText(ViewPaper2Page.this, "בקשת משחק נשלחה לכל החברים!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ViewPaper2Page.this, "אין לך חברים לשלוח אליהם בקשה!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // בדיקה אם החבר מאפשר קבלת הודעות ושליחת בקשה במידת הצורך
    private void checkAndSendNotification(String friendId) {
        DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference("Users").child(friendId);
        friendRef.child("allowNotifications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean allowNotifications = snapshot.getValue(Boolean.class);
                if (allowNotifications != null && allowNotifications) {
                    sendNotificationToUser(friendId, "משתמש רוצה לשחק איתך עכשיו!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // שליחת הודעה לחבר
    private void sendNotificationToUser(String userId, String message) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(userId);
        String notificationId = notificationsRef.push().getKey();

        // הוספת לוג לפני שליחת ההודעה
        Log.d("Notification", "Sending notification to " + userId + " with message: " + message);

        notificationsRef.child(notificationId).setValue(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // לוג הצלחה אחרי שההודעה נשלחה בהצלחה
                Log.d("Notification", "Notification sent successfully to " + userId);
            } else {
                // לוג של כשלון אם נכשלה שליחת ההודעה
                Log.e("Notification", "Failed to send notification to " + userId, task.getException());
            }
        });
    }

    // הגדרת כפתור הפעלת/כיבוי התראות
    private void setupAllowNotifications() {
        userRef.child("allowNotifications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean allow = snapshot.getValue(Boolean.class);
                allowNotifications.setChecked(allow != null && allow);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        allowNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                userRef.child("allowNotifications").setValue(isChecked)
        );
    }

    // מאזין להתראות חדשות מה-Database
    private void listenForNotifications() {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(userId);

        notificationsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String message = snapshot.getValue(String.class);
                if (message != null) {
                    showNotification(message);
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // הצגת התראה למשתמש
    private void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "play_request_channel")
                .setSmallIcon(R.drawable.avatar)
                .setContentTitle("PlayPal")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(new Random().nextInt(), builder.build());

    }

    // הגדרת כפתורי ניווט בדפים
    private void setupPageNavigation() {
        findViewById(R.id.page1_button).setOnClickListener(v -> viewPager2.setCurrentItem(0));
        findViewById(R.id.page2_button).setOnClickListener(v -> viewPager2.setCurrentItem(1));
        findViewById(R.id.page3_button).setOnClickListener(v -> viewPager2.setCurrentItem(2));
    }
}