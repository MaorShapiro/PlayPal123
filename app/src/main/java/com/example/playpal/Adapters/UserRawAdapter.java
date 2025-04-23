package com.example.playpal.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.playpal.Activities.UserPage;
import com.example.playpal.Utills.ImageUtil;

import com.example.playpal.Models.User;
import com.example.playpal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserRawAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    public static String imageProfile = "";

    public UserRawAdapter(Context context, List<User> users) {
        super(context, R.layout.activity_user_raw, users);
        this.context = context;
        this.users = users;
    }

    public class ViewHolder {
        TextView tvUname;
        TextView tvGame;
        TextView tvLevel;
        ImageView imageView;
        ImageButton btnProfile;
        ImageButton btnAddFriend;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.activity_user_raw, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvUname = convertView.findViewById(R.id.tvUserName);
            viewHolder.tvGame = convertView.findViewById(R.id.tvGame);
            viewHolder.tvLevel = convertView.findViewById(R.id.tvLevel);
            viewHolder.imageView = convertView.findViewById(R.id.IVpfp);
            viewHolder.btnProfile = convertView.findViewById(R.id.btnProfile);
            viewHolder.btnAddFriend = convertView.findViewById(R.id.btnAddFriend);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        User currentUser = users.get(position);

        if (currentUser != null) {
            if (currentUser.getImageBase64() != null && !currentUser.getImageBase64().isEmpty()) {
                // המרת ה-Base64 ל-Bitmap
                Bitmap bitmap = ImageUtil.convertFrom64base(currentUser.getImageBase64());
                if (bitmap != null) {
                    // הצגת התמונה ב-ImageView
                    viewHolder.imageView.setImageBitmap(bitmap);
                } else {
                    // אם ההמרה נכשלה, הצג תמונה ברירת מחדל
                    viewHolder.imageView.setImageResource(R.drawable.avatar);
                }
            } else {
                // אם אין תמונה ב-Base64, הצג תמונה ברירת מחדל
                viewHolder.imageView.setImageResource(R.drawable.avatar);
            }

           }


            viewHolder.tvUname.setText(currentUser.getUsername());
            viewHolder.tvGame.setText(currentUser.getCurrentGame());
            viewHolder.tvLevel.setText(currentUser.getGetCurrentLevel());


            viewHolder.btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(context, UserPage.class);
                intent.putExtra("userName", viewHolder.tvUname.getText().toString());
                intent.putExtra("game", viewHolder.tvGame.getText().toString());
                intent.putExtra("userLevel", viewHolder.tvLevel.getText().toString());
                intent.putExtra("country", currentUser.getCountry());
                intent.putExtra("languages", currentUser.getLanguage());
                //intent.putExtra("image", currentUser.getImageBase64() != null ? currentUser.getImageBase64() : "");
                intent.putExtra("receiverId", currentUser.getId());

                intent.putExtra("adapterSource", "UserRawAdapter");
                imageProfile = currentUser.getImageBase64() != null ? currentUser.getImageBase64() : "";

                context.startActivity(intent);
            });

            viewHolder.btnAddFriend.setOnClickListener(v -> {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String selectedUserId = currentUser.getId();

                DatabaseReference currentUserRef = myRef.child(currentUserId);
                DatabaseReference selectedUserRef = myRef.child(selectedUserId);

                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        if (user != null) {
                            user.addFriend(selectedUserId);
                            currentUserRef.setValue(user);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });

                selectedUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User selectedUser = dataSnapshot.getValue(User.class);

                        if (selectedUser != null) {
                            selectedUser.addFriend(currentUserId);
                            selectedUserRef.setValue(selectedUser).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Friend added successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Failed to add friend.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
            });


        return convertView;
    }
}