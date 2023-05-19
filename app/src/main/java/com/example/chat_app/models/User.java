package com.example.chat_app.models;

import android.content.Context;

import com.example.chat_app.utilities.PreferenceManager;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    public String name, image, email, token, id, birthDate, gender, phoneNumber;
    public User(){}


}
