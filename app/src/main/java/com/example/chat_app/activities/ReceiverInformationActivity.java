package com.example.chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivityReceiverInformationBinding;
import com.example.chat_app.models.User;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.Render;

public class ReceiverInformationActivity extends AppCompatActivity {
    User receiver;
    private ActivityReceiverInformationBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiverInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initInformation();
    }

    private void initInformation() {
        receiver = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        Render.renderingBitmap(binding.imageProfile,receiver.image);
        binding.textBirthDate.setText(receiver.birthDate);
        binding.textGender.setText(receiver.gender);
        binding.textEmail.setText(receiver.email);
        binding.textName.setText(receiver.name);
        binding.textPhoneNumber.setText(receiver.phoneNumber == null ? "No data" : receiver.phoneNumber);
    }
}