package com.example.chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));


    }
}