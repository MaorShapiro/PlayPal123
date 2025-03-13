package com.example.playpal.Adapters;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playpal.Activities.LoginPage;
import com.example.playpal.Activities.SetPfpPage;
import com.example.playpal.Models.User;
import com.example.playpal.R;
import com.example.playpal.Activities.Results_Page;
import com.example.playpal.Activities.SignPage;
import com.example.playpal.Utills.ImageUtil;
import com.example.playpal.Utills.ValidatorUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewPager2Adapter extends RecyclerView.Adapter<ViewPager2Adapter.ViewHolder> {

    //האדפטר של כל הדפים בviewpaper2
    private Context context;
    public ViewPager2Adapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == 0) {
            // עמוד 1
            view = LayoutInflater.from(context).inflate(R.layout.activity_main_page, parent, false);
        } else if (viewType == 1) {
            // עמוד 2
            view = LayoutInflater.from(context).inflate(R.layout.activity_search_page, parent, false);
        } else {
            // עמוד 3
            view = LayoutInflater.from(context).inflate(R.layout.activity_update_user_profile_page, parent, false);
        }
        //ImageUtil.requestPermission(context);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 1) {
            // תוכן של SearchPage
            //games spinner
            Spinner gameSpinner = holder.itemView.findViewById(R.id.game_spinner);
            String[] games = {"Fifa", "Minecraft", "League of Legends","Fall Guys","Fortnite","JerkMate","Roblox","Valorant"};
            ArrayAdapter<String> gameAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, games);
            gameSpinner.setAdapter(gameAdapter);
            //levels spinner
            Spinner levelSpinner = holder.itemView.findViewById(R.id.level_spinner);
            String[] levels = {"Beginner", "Intermediate", "Expert"};
            ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, levels);
            levelSpinner.setAdapter(levelAdapter);
            //countries spinner
            Spinner countriesSpinner = holder.itemView.findViewById(R.id.country_spinner);
            String[] countries = {"Israel", "USA", "UK", "Canada"};
            ArrayAdapter<String> countriesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, countries);
            countriesSpinner.setAdapter(countriesAdapter);
            //languages spinner
            Spinner languagesSpinner = holder.itemView.findViewById(R.id.language_spinner);
            String[] languages = {"Hebrew", "English", "French", "Spanish"};
            ArrayAdapter<String> languagesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, languages);
            languagesSpinner.setAdapter(languagesAdapter);
            //search button
            holder.searchButton.setOnClickListener(v -> {
                String selectedGame = gameSpinner.getSelectedItem().toString();
                String selectedLevel = levelSpinner.getSelectedItem().toString();
                String selectedCountry = countriesSpinner.getSelectedItem().toString();
                String selectedLanguage = languagesSpinner.getSelectedItem().toString();


                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @NonNull
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // קבלת נתוני המשתמש מהפיירבייס
                            User existingUser = dataSnapshot.getValue(User.class);

                            // בדיקת שינויים לאחד משתני המשתמש
                            boolean isUpdated = false;
                            if (!selectedGame.equals(existingUser.getCurrentGame())) {
                                isUpdated = true;
                                existingUser.setCurrentGame(selectedGame);
                                ;
                            }
                            if (!selectedLevel.equals(existingUser.getGetCurrentLevel())) {
                                isUpdated = true;
                                existingUser.setGetCurrentLevel(selectedLevel);
                            }
                            if (!selectedCountry.equals(existingUser.getCountry())) {
                                isUpdated = true;
                                existingUser.setCountry(selectedCountry);
                            }
                            if (!selectedLanguage.equals(existingUser.getLanguage())) {
                                isUpdated = true;
                                existingUser.setLanguage(selectedLanguage);
                            }


                            // אם יש שינוי מעדכנים את הנתונים בFirebase
                            if (isUpdated) {
                                userRef.setValue(existingUser).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "הפרטים עודכנו בהצלחה!", Toast.LENGTH_LONG).show();
                                        Log.d(TAG, "User updated: " + existingUser);
                                    } else {
                                        Toast.makeText(context, "עדכון נתונים נכשל", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Toast.makeText(context, "לא היו שינויים", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Failed to read user data", databaseError.toException());
                        }
                    });
                }
                Log.d("CustomPagerAdapter", "Selected Game: " + selectedGame);


                Intent intent = new Intent(context, Results_Page.class);
                intent.putExtra("selectedGame", selectedGame);
                intent.putExtra("selectedLevel", selectedLevel);
                intent.putExtra("selectedCountry", selectedCountry);
                intent.putExtra("selectedLanguage", selectedLanguage);
                context.startActivity(intent);

                Toast.makeText(context, "Searching for " + selectedGame + " (" + selectedLevel + ")" + selectedCountry + " (" + selectedLanguage + ")", Toast.LENGTH_SHORT).show();

            });

        } else if (position == 0) {
            // תוכן של MainActivity2
            Toast.makeText(context, "Page 1", Toast.LENGTH_SHORT).show();

            if (holder.tvGreeting != null) {
                // הצגת שלום לשם משתמש
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    // קריאה למידע על המשתמש מתוך הFirebase
                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            String userName;
                            if (user != null) {
                                userName = user.getUsername();
                            } else {
                                userName = null;
                            }

                            if (userName == null || userName.isEmpty()) {
                                // אם אין שם משתמש, הצג את המייל
                                userName = currentUser.getEmail();
                            }

                            // הצגת שם המשתמש או המייל ב TextView
                            holder.tvGreeting.setText("שלום " + userName);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("FirebaseError", "Failed to load user data", databaseError.toException());
                        }
                    });
                } else {
                    holder.tvGreeting.setText("שלום, אנא התחבר!");
                }
            }

            // יצירת ה-Adapter ורשימת החברים מחוץ ל-if


            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            List<String> friendIds = user.getFriends();
                            List<User> friendDetailsList = new ArrayList<>();

                            // צור את ה-Adapter מחוץ ללולאה
                            FriendRawAdapter adapter = new FriendRawAdapter(context, friendDetailsList);
                            holder.listView.setAdapter(adapter);

                            if (friendIds != null) {
                                friendDetailsList.clear(); // נקה את הרשימה לפני טעינת נתונים חדשים
                                for (String friendId : friendIds) {
                                    DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference("Users").child(friendId);
                                    friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            User friend = dataSnapshot.getValue(User.class);
                                            if (friend != null) {
                                                friendDetailsList.add(friend);
                                                adapter.notifyDataSetChanged(); // עדכן את ה-ListView לאחר שינוי
                                                Log.d(TAG, " " + user.getFriends().size());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.e("FirebaseError", "Failed to load friend data", databaseError.toException());
                                        }
                                    });
                                }
                            } else {
                                friendDetailsList.clear();
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", "Failed to load user data", databaseError.toException());
                    }
                });
            }
        } else if (position == 2) {
            // עמוד 3: תוכן של MainActivity3
            Toast.makeText(context, "Page 3", Toast.LENGTH_SHORT).show();

            ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(context, R.array.age_options, android.R.layout.simple_spinner_item);
            ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spAge.setAdapter(ageAdapter);

            ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(context, R.array.country_options, android.R.layout.simple_spinner_item);
            countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spCountry.setAdapter(countryAdapter);

            ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(context, R.array.language_options, android.R.layout.simple_spinner_item);
            languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spLanguage.setAdapter(languageAdapter);

            holder.userImageView.setOnClickListener(v -> {
                Intent intent = new Intent(context, SetPfpPage.class);
                context.startActivity(intent);

            });




        // פעולה לכפתור השמירה
        holder.btnSubmit.setOnClickListener(v -> {
            String userName = holder.etUserName.getText().toString().trim();
            String email = holder.etEmail.getText().toString().trim();
            String password = holder.etPassword.getText().toString().trim();
            String phone = holder.etPhone.getText().toString().trim();

            int selectedGenderId = holder.rgGender.getCheckedRadioButtonId();
            RadioButton selectedGender = holder.itemView.findViewById(selectedGenderId);
            String gender = selectedGender != null ? selectedGender.getText().toString() : "לא נבחר";

            String age = holder.spAge.getSelectedItem().toString();
            String country = holder.spCountry.getSelectedItem().toString();
            String language = holder.spLanguage.getSelectedItem().toString();



            // בדיקות אם השדות לא ריקים ולטפל ב-null
            if (userName == null || userName.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()) {
                Toast.makeText(context, "יש למלא את כל השדות החיוניים!", Toast.LENGTH_LONG).show();
                return;
            }

            // שימוש ב-ValidatorUtil לבדיקת תקינות הערכים
            if (!ValidatorUtil.isNameValid(userName)) {
                Toast.makeText(context, "שם המשתמש לא תקין! יש לוודא שהשם לפחות 3 תוים.", Toast.LENGTH_LONG).show();
                return;
            }
            if (!ValidatorUtil.isEmailValid(email)) {
                Toast.makeText(context, "כתובת המייל לא תקינה!", Toast.LENGTH_LONG).show();
                return;
            }
            if (!ValidatorUtil.isPasswordValid(password)) {
                Toast.makeText(context, "הסיסמה חייבת להיות באורך 6 תוים לפחות!", Toast.LENGTH_LONG).show();
                return;
            }
            if (!ValidatorUtil.isPhoneValid(phone)) {
                Toast.makeText(context, "מספר הטלפון לא תקין!", Toast.LENGTH_LONG).show();
                return;
            }

            // כאן תוסיף את הבדיקה אם הערכים שונו בהשוואה לערכים הקיימים
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // קבלת נתוני המשתמש מהמאגר
                        holder.existingUser = dataSnapshot.getValue(User.class);

                        // בדיקת שינוי בכל שדה
                        boolean isUpdated = false;
                        if (holder.existingUser != null) {
                            if (userName != null && !userName.equals(holder.existingUser.getUsername())) {
                                isUpdated = true;
                                holder.existingUser.setUsername(userName);
                            }

                            if (email != null && !email.equals(holder.existingUser.getEmail())) {
                                isUpdated = true;
                                holder.existingUser.setEmail(email);
                            }
                            if (password != null && !password.equals(holder.existingUser.getPassword())) {
                                isUpdated = true;
                                holder.existingUser.setPassword(password);
                            }
                            if (phone != null && !phone.equals(holder.existingUser.getPhone())) {
                                isUpdated = true;
                                holder.existingUser.setPhone(phone);
                            }
                            if (gender != null && !gender.equals(holder.existingUser.getGender())) {
                                isUpdated = true;
                                holder.existingUser.setGender(gender);
                            }
                            if (age != null && !age.equals(holder.existingUser.getAge())) {
                                isUpdated = true;
                                holder.existingUser.setAge(age);
                            }
                            if (country != null && !country.equals(holder.existingUser.getCountry())) {
                                isUpdated = true;
                                holder.existingUser.setCountry(country);
                            }
                            if (language != null && !language.equals(holder.existingUser.getLanguage())) {
                                isUpdated = true;
                                holder.existingUser.setLanguage(language);
                            }


                            // אם יש שינוי, מעדכנים את הנתונים ב-Firebase
                            if (isUpdated) {
                                userRef.setValue(holder.existingUser).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "הפרטים עודכנו בהצלחה!", Toast.LENGTH_LONG).show();
                                        Log.d(TAG, "User updated: " + holder.existingUser);
                                    } else {
                                        Toast.makeText(context, "עדכון נתונים נכשל", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Toast.makeText(context, "לא היו שינויים", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to read user data", databaseError.toException());
                    }
                });
            }
        });
    }}


    @Override
    public int getItemCount() {
        return 3; // מספר העמודים
    }

    @Override
    public int getItemViewType(int position) {
        // מחזיר את ה-viewType עבור כל עמוד
        return position;
    }


















    public static class ViewHolder extends RecyclerView.ViewHolder {
        Spinner gameSpinner, levelSpinner, countrySpinner, languageSpinner;
        Button searchButton,btnLogin, btnRegister;
        EditText etUserName, etEmail, etPassword, etPhone, etGame1, etGame2, etGame3;
        RadioGroup rgGender;
        Spinner spAge, spCountry, spLanguage;
        Button btnSubmit;
        TextView tvGreeting;
        ListView listView;
        ImageView userImageView;
        User existingUser;


        public ViewHolder(View itemView) {
            super(itemView);
            // רק אם יש את ה-Views, יתחברו להם
            gameSpinner = itemView.findViewById(R.id.game_spinner);
            levelSpinner = itemView.findViewById(R.id.level_spinner);
            countrySpinner = itemView.findViewById(R.id.country_spinner);
            languageSpinner = itemView.findViewById(R.id.language_spinner);
            searchButton = itemView.findViewById(R.id.search_button);
            etUserName = itemView.findViewById(R.id.et_user_name);
            etEmail = itemView.findViewById(R.id.et_email);
            etPassword = itemView.findViewById(R.id.et_password);
            etPhone = itemView.findViewById(R.id.et_phone);
            rgGender = itemView.findViewById(R.id.rg_gender);
            spAge = itemView.findViewById(R.id.sp_age);
            spCountry = itemView.findViewById(R.id.sp_country);
            spLanguage = itemView.findViewById(R.id.sp_language);
            listView = itemView.findViewById(R.id.friends_listView);
            btnSubmit = itemView.findViewById(R.id.btn_submit);
            tvGreeting = itemView.findViewById(R.id.tvGreeting);
            userImageView = itemView.findViewById(R.id.imageView2);
        }
    }}




