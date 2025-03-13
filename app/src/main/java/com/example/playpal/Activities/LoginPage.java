package com.example.playpal.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.playpal.Utills.ValidatorUtil;
import com.example.playpal.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity implements View.OnClickListener {
    Button btnLogin;
    EditText etEmail, etPassword;

    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;

    public static final String MyPREFERENCES = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);

        initViews();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        FirebaseApp.initializeApp(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btnLogin) {
            login();
        }
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // אימות שדות
        if (!ValidatorUtil.isEmailValid(email)) {
            Toast.makeText(this, "כתובת המייל לא תקינה!", Toast.LENGTH_LONG).show();
            return;
        }
        if (!ValidatorUtil.isPasswordValid(password)) {
            Toast.makeText(this, "הסיסמה חייבת להיות באורך 6 תוים לפחות!", Toast.LENGTH_LONG).show();
            return;
        }

        // ניסיון התחברות עם Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // התחברות מוצלחת
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // שמירת פרטי המשתמש ב-SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.apply();

                            Toast.makeText(this, "התחברות בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();

                            // מעבר לדף הבא (ViewPaper2Page)
                            Intent goNext = new Intent(getApplicationContext(), ViewPaper2Page.class);
                            startActivity(goNext);
                        }
                    } else {
                        // התחברות נכשלה
                        Toast.makeText(this, "התחברות נכשלה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}