package com.example.playpal.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.playpal.Adapters.ImageSourceAdapter;
import com.example.playpal.Adapters.ViewPager2Adapter;
import com.example.playpal.Models.User;
import com.example.playpal.R;
import com.example.playpal.Utills.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class SetPfpPage extends AppCompatActivity {
    private ActivityResultLauncher<Intent> selectImageLauncher;
    private ActivityResultLauncher<Intent> captureImageLauncher;
    ImageView userImageView;
    FirebaseUser currentUser;
    int SELECT_PICTURE=200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_pfp_page);
        ImageUtil.requestPermission(this);
        userImageView = findViewById(R.id.userImageView);
        userImageView.setOnClickListener(v -> showImageSourceDialog(new ViewPager2Adapter.ViewHolder(userImageView)));
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // המשתמש לא מחובר
            return;
        }
        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        Bitmap bitmap = ((BitmapDrawable) userImageView.getDrawable()).getBitmap();
                        String base64Image = ImageUtil.convertTo64Base(userImageView);
                        updateProfileImage(base64Image);
                        finish();
                    }
                });

        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        userImageView.setImageBitmap(bitmap);
                        String base64Image = ImageUtil.convertTo64Base(userImageView);
                        updateProfileImage(base64Image);
                        finish();
                    }
                });
    }


    private void updateProfileImage(String base64Image) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Users").child(currentUser.getUid());

        // עדכון התמונה ב-Base64
        userRef.child("imageBase64").setValue(base64Image)
                .addOnSuccessListener(aVoid -> Log.d("Profile", "Image updated successfully"))
                .addOnFailureListener(e -> Log.w("Profile", "Error updating image", e));
    }

    private void showImageSourceDialog(ViewPager2Adapter.ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");

        final ArrayList<Map.Entry<String, Integer>> options = new ArrayList<>();
        options.add(Map.entry("Gallery", R.drawable.gallery_thumbnail));
        options.add(Map.entry("Camera", R.drawable.photo_camera));

        ImageSourceAdapter adapter = new ImageSourceAdapter(this, options);

        builder.setAdapter(adapter, (dialog, index) -> {
            if (index == 0) {
                selectImageFromGallery();
            } else if (index == 1) {
                captureImageFromCamera();
            }
        });
        builder.show();
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectImageLauncher.launch(intent);
        imageChooser();
    }

    private void captureImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageLauncher.launch(takePictureIntent);
    }
    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    userImageView.setImageURI(selectedImageUri);
                }
            }}
    }}

