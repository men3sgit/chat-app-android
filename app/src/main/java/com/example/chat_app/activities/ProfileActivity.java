package com.example.chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivityProfileBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.Render;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUI();

    }

    private void initUI() {
        Intent intent = getIntent();
        Render.renderingBitmap(binding.imageProfile,intent.getStringExtra(Constants.KEY_IMAGE));
        binding.textName.setText(intent.getStringExtra(Constants.KEY_NAME));
        binding.textBirthDate.setText(intent.getStringExtra(Constants.KEY_BIRTH_DATE));
        binding.textEmail.setText(intent.getStringExtra(Constants.KEY_EMAIL));
        binding.textGender.setText(intent.getStringExtra(Constants.KEY_GENDER));
        final String phoneNumber = intent.getStringExtra(Constants.KEY_PHONE_NUMBER);
        binding.textPhoneNumber.setText(phoneNumber == null ? "No data" : phoneNumber);

    }

}