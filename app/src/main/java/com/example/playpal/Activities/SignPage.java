package com.example.playpal.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.playpal.Utills.ImageUtil;
import com.example.playpal.Utills.ValidatorUtil;

import com.example.playpal.Models.User;
import com.example.playpal.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignPage extends AppCompatActivity implements View.OnClickListener {
    Button btnRegister;
    EditText etUserName, etEmail, etPasword, etPhone;


    FirebaseDatabase database;

    DatabaseReference myRef;


    FirebaseAuth mAuth;

    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_page);


        initViews();




    }

    private void initViews() {

        etUserName = findViewById(R.id.etUserName);;

        etEmail = findViewById(R.id.etEmail);
        etPasword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        FirebaseApp.initializeApp(this);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


    }


    @Override
    public void onClick(View view) {
        if (view == btnRegister) {

            save();
        }

    }

    public void save() {
        Boolean isValid = true;
        String userName = etUserName.getText().toString();

        String email = etEmail.getText().toString();
        String password = etPasword.getText().toString();
        String phone = etPhone.getText().toString();
        if (userName.isEmpty() || email.isEmpty() ||
                password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (!ValidatorUtil.isNameValid(userName)) {
            Toast.makeText(this, "שם המשתמש לא תקין! יש לוודא שהשם לפחות 3 תוים.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!ValidatorUtil.isPhoneValid(phone)) {
            Toast.makeText(this, "מספר הטלפון לא תקין!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!ValidatorUtil.isEmailValid(email)) {
            Toast.makeText(this, "כתובת המייל לא תקינה!", Toast.LENGTH_LONG).show();
            return;
        }
        if (!ValidatorUtil.isPasswordValid(password)) {
            Toast.makeText(this, "הסיסמה חייבת להיות באורך 6 תוים לפחות!", Toast.LENGTH_LONG).show();
            return;
        }

        if (isValid) {
            Toast.makeText(this, "הרשמה בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d("TAG", "createUserWithEmail:success");

                            FirebaseUser fireuser = mAuth.getCurrentUser();
                            if (fireuser != null) {
                                User newUser = new User(fireuser.getUid(), userName, email, password, phone, "male",true);
                                // Add user to Firebase Database
                                myRef.child(fireuser.getUid()).setValue(newUser).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString("email", email);
                                        editor.putString("password", password);
                                        editor.apply();

                                        // Navigate to next page
                                        Intent goLog = new Intent(getApplicationContext(), ViewPaper2Page.class);
                                        startActivity(goLog);
                                        finish(); // Close current activity
                                    } else {
                                        Log.e("TAG", "Failed to save user to database: " + task1.getException());
                                        Toast.makeText(this, "שמירת פרטים נכשלה", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            // If sign in fails
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(this, "הרשמה נכשלה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}