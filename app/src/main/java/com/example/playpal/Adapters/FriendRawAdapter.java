package com.example.playpal.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.playpal.Activities.UserPage;
import com.example.playpal.Models.User;
import com.example.playpal.R;
import com.example.playpal.Utills.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
//הקוד של הדף של השורה של חבר
public class FriendRawAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> friendsList;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    public static String imageProfule="";

    public FriendRawAdapter(Context context, List<User> friendsList) {
        super(context, R.layout.activity_friend_raw, friendsList);
        this.context = context;
        this.friendsList = friendsList;
    }

    public class ViewHolder {
        TextView tvUname;
        ImageView imageView;
        ImageButton btnProfile;
        ImageButton btnRemoveFriend;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.activity_friend_raw, parent, false);
        }
        ViewHolder viewHolder;

        viewHolder = new ViewHolder();
        viewHolder.tvUname = convertView.findViewById(R.id.tvUserName);
        viewHolder.imageView = convertView.findViewById(R.id.IVpfp);
        viewHolder.btnProfile = convertView.findViewById(R.id.btnProfile);
        viewHolder.btnRemoveFriend = convertView.findViewById(R.id.btnRemoveFriend);

        User friend = friendsList.get(position);
        viewHolder.tvUname.setText(friend.getUsername());
        if (friend != null) {
            if (friend.getImageBase64() != null && !friend.getImageBase64().isEmpty()) {
                // המרת ה-Base64 ל-Bitmap
                Bitmap bitmap = ImageUtil.convertFrom64base(friend.getImageBase64());
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

        viewHolder.btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserPage.class);
            intent.putExtra("userName", viewHolder.tvUname.getText().toString());
            intent.putExtra("country", friend.getCountry());
            intent.putExtra("userLevel", friend.getCurrentLevel());
            intent.putExtra("game", friend.getCurrentGame());
            intent.putExtra("languages", friend.getLanguage());
           // intent.putExtra("image", friend.getImageBase64().toString());




            imageProfule=friend.getImageBase64().toString();

                    // != null ? friend.getImageBase64() : "");
            intent.putExtra("receiverId", friend.getId());
            context.startActivity(intent);
        });

        viewHolder.btnRemoveFriend.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String selectedUserId = friend.getId();

            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
            DatabaseReference selectedUserRef = FirebaseDatabase.getInstance().getReference("Users").child(selectedUserId);

            // הסרת החבר מרשימת המשתמש הנוכחי
            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getFriends() != null) {
                        user.getFriends().remove(selectedUserId);
                        currentUserRef.setValue(user).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Friend removed successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to remove friend.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "Error removing friend", databaseError.toException());
                }
            });

            // הסרת המשתמש מרשימת החברים של החבר הנבחר
            selectedUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User selectedUser = dataSnapshot.getValue(User.class);

                    if (selectedUser != null && selectedUser.getFriends() != null) {
                        selectedUser.getFriends().remove(currentUserId);
                        selectedUserRef.setValue(selectedUser).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("Firebase", "Removed from friend's list successfully");
                            } else {
                                Log.e("FirebaseError", "Failed to remove friend from their list");
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("FirebaseError", "Error updating friend's list", error.toException());
                }
            });
        });

        return convertView;
    }
}
