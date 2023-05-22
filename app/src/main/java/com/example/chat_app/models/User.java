package com.example.chat_app.models;

import android.content.Context;

import com.example.chat_app.utilities.PreferenceManager;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    public String name, image, email, token, id, birthDate, gender, phoneNumber;
    public User(){}

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", id='" + id + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", gender='" + gender + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
