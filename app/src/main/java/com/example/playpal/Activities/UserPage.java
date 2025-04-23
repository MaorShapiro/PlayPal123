package com.example.playpal.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playpal.Adapters.FriendRawAdapter;
import com.example.playpal.Adapters.UserRawAdapter;
import com.example.playpal.Utills.ImageUtil;
import com.example.playpal.Adapters.PrivateChatAdapter;
import com.example.playpal.Models.Message;
import com.example.playpal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserPage extends AppCompatActivity implements View.OnClickListener {
    private EditText messageInput;
    private ImageButton ibSendButton;
    private RecyclerView chatRecyclerView;
    private PrivateChatAdapter privateChatAdapter;
    private List<Message> messages;
    private TextView tvUname, tvGame, tvLevel,tvCountry,tvLanguages;
    private ImageView pfp;

    private String senderId, receiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);
        senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // קישור רכיבים מה-XML
        ibSendButton = findViewById(R.id.sendButton);
        messageInput = findViewById(R.id.messageInput);
        tvUname = findViewById(R.id.userName);
        tvGame = findViewById(R.id.tvFavoriteGame);
        tvLevel = findViewById(R.id.tvLevel);
        tvCountry = findViewById(R.id.tvCountry);
        tvLanguages = findViewById(R.id.tvLanguages);
        pfp = findViewById(R.id.profilePicture);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        messages = new ArrayList<>();
        privateChatAdapter = new PrivateChatAdapter(messages, senderId);
        chatRecyclerView.setAdapter(privateChatAdapter);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        ibSendButton.setOnClickListener(this);

        // קבלת נתוני המשתמש מה-Intent
        String userName = getIntent().getStringExtra("userName");
        String game = getIntent().getStringExtra("game");
        String userLevel = getIntent().getStringExtra("userLevel");
        String country = getIntent().getStringExtra("country");
        String language = getIntent().getStringExtra("languages");
        String adapterSource = getIntent().getStringExtra("adapterSource");
        receiverId = getIntent().getStringExtra("receiverId");

        tvUname.setText(userName);
        tvGame.setText("Game: "+game);
        tvLevel.setText("Level: "+userLevel);
        tvCountry.setText("Country: "+country);
        tvLanguages.setText("Language: "+language);



        if ("FriendRawAdapter".equals(adapterSource)) {
            // הגעת מ-FriendRawAdapter, הצג את התמונה מ-FriendRawAdapter
            String image = FriendRawAdapter.imageProfile;
            if (image != null && !image.isEmpty()) {
                Bitmap decodedBitmap = ImageUtil.convertFrom64base(image);
                if (decodedBitmap != null) {
                    pfp.setImageBitmap(decodedBitmap);
                } else {
                    Log.e("ImageError", "Failed to decode Base64 image");
                    pfp.setImageResource(R.drawable.avatar);
                }
            }
        } else if ("UserRawAdapter".equals(adapterSource)) {
            // הגעת מ-UserRawAdapter, הצג את התמונה מ-UserRawAdapter
            String image2 = UserRawAdapter.imageProfile;
            if (image2 != null && !image2.isEmpty()) {
                Bitmap decodedBitmap = ImageUtil.convertFrom64base(image2);
                if (decodedBitmap != null) {
                    pfp.setImageBitmap(decodedBitmap);
                } else {
                    Log.e("ImageError", "Failed to decode Base64 image");
                    pfp.setImageResource(R.drawable.avatar);
                }
            }
        }

        // טעינת הודעות קיימות
        loadMessages();
    }

    private void loadMessages() {
        String chatId = getChatId(senderId, receiverId);
        DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("Messages")
                .child(chatId);

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);

                    if (message != null &&
                            (message.getSender() != null && message.getReceiver() != null) &&
                            ((message.getSender().equals(senderId) && message.getReceiver().equals(receiverId)) ||
                                    (message.getSender().equals(receiverId) && message.getReceiver().equals(senderId)))) {

                        messages.add(message);
                    }
                }
                privateChatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserPage.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == ibSendButton) {
            sendMessage();
        }
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("Messages");
            String chatId = getChatId(senderId, receiverId);

            Message message = new Message(senderId, receiverId, messageText, System.currentTimeMillis());

            messageRef.child(chatId).push().setValue(message)
                    .addOnSuccessListener(aVoid -> messageInput.setText(""));
        }
    }
    private String getChatId(String senderId, String receiverId) {
        if (senderId.compareTo(receiverId) < 0) {
            return senderId + "_" + receiverId;
        } else {
            return receiverId + "_" + senderId;
        }
    }
}


