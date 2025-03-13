package com.example.playpal.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import com.example.playpal.Utills.ImageUtil;
import com.example.playpal.R;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
   private  String id;
    private String username;
    private String email;
    private String password;
    private String phone;
    private String gender;
    private String age;
    private String country;
    private String language;
    private List<String> friends = new ArrayList<>();
    private String currentGame;
    private String CurrentLevel;
    private String imageBase64;
    public boolean allowNotifications;

    public User(String id, String username, String email, String password, String phone, String gender, String age, String country, String language, List<String> friends, String currentGame, String currentLevel,String imageBase64) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.age = age;
        this.country = country;
        this.language = language;
        this.friends = friends;
        this.currentGame = currentGame;
        this.CurrentLevel = currentLevel;
        this.imageBase64=imageBase64;
    }

    public String getGetCurrentLevel() {
        return CurrentLevel;
    }

    public void setGetCurrentLevel(String getCurrentLevel) {
        this.CurrentLevel = getCurrentLevel;
    }

    public String getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame = currentGame;
    }

    public User() {
    }



    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public User(String id, String username, String email, String password, String phone, String gender,boolean AllowNotifications) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.country = "Israel";
        this.language = "Hebrew";
        this.currentGame="Fifa";
        this.CurrentLevel="Beginner";
        this.allowNotifications=AllowNotifications;
        this.imageBase64="";
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<String> getFriendsArray() {
        return friends;
    }


    public void setFriendsArray(List<String> gameArray) {
        this.friends = gameArray;
    }

    public String getCurrentLevel() {
        return CurrentLevel;
    }

    public void setCurrentLevel(String currentLevel) {
        CurrentLevel = currentLevel;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    // המרה של תמונה ל-Base64
    public void setImageFromImageView(ImageView imageView) {
        this.imageBase64 = ImageUtil.convertTo64Base(imageView);
    }
    public void addFriend(String friendId) {
        if (this.friends == null) {
            this.friends = new ArrayList<>();
        }
        if (!this.friends.contains(friendId)) {
            this.friends.add(friendId);
        }
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public boolean isAllowNotifications(){
        return allowNotifications;
    }
    public void setAllowNotification(boolean allowNotification){
        this.allowNotifications=allowNotification;
    }


    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", age='" + age + '\'' +
                ", country='" + country + '\'' +
                ", language='" + language + '\'' +
                ", gameArray=" + friends +
                ", currentGame='" + currentGame + '\'' +
                ", CurrentLevel='" + CurrentLevel + '\'' +
                '}';
    }
}