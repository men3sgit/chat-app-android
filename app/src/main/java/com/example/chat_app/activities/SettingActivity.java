package com.example.chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivityChatBinding;
import com.example.chat_app.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity {
    ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }
    private void setListeners() {
        binding.layoutChangePassword.setOnClickListener(view -> {
            Intent intent = new Intent(SettingActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }


}