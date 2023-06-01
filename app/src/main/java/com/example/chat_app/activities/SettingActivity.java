package com.example.chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivityChatBinding;
import com.example.chat_app.databinding.ActivitySettingBinding;
import com.example.chat_app.utilities.PreferenceManager;

public class SettingActivity extends AppCompatActivity {
    ActivitySettingBinding binding;
    private static final int DARK_MODE = 0;
    private static final int LIGHT_MODE = 1;
    public static int currentMode;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    boolean nightMode = false;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        currentMode = LIGHT_MODE;
        preferenceManager = new PreferenceManager(getApplicationContext());
        nightMode = preferenceManager.getBoolean("NIGHT_MODE");
        System.out.println("NIGHT MODE + " + nightMode);
        if(nightMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            updateMode(DARK_MODE);
        }else{
            updateMode(LIGHT_MODE);
        }


    }


    private void setListeners() {
        binding.layoutChangePassword.setOnClickListener(view -> {
            Intent intent = new Intent(SettingActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
        binding.buttonBack.setOnClickListener(view -> onBackPressed());
        binding.layoutLightMode.setOnClickListener(view -> {
            updateMode(LIGHT_MODE);
        });
        binding.checkLight.setOnClickListener(view -> {
            updateMode(LIGHT_MODE);
        });
        binding.layoutDarkMode.setOnClickListener(view -> {
            updateMode(DARK_MODE);
        });
        binding.checkDark.setOnClickListener(view -> {
            updateMode(DARK_MODE);
        });
    }

    public void updateMode(int mode) {
        if (mode == LIGHT_MODE) {
            binding.checkLight.setChecked(true);
            binding.checkDark.setChecked(false);
            preferenceManager.putBoolean("NIGHT_MODE",false);

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        } else {
            binding.checkDark.setChecked(true);
            binding.checkLight.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            preferenceManager.putBoolean("NIGHT_MODE",true);

        }
        this.currentMode = mode;
    }

}