package com.example.chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivityReceiverInformationBinding;
import com.example.chat_app.models.User;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.Render;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ReceiverInformationActivity extends BaseActivity {
    User receiver;
    private Boolean isReceiverAvailable = false;
    private ActivityReceiverInformationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiverInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initInformation();
        setListeners();
    }

    private void setListeners() {
        binding.buttonBackToChat.setOnClickListener(view -> finish());
    }

    private void initInformation() {
        receiver = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        Render.renderingBitmap(binding.imageProfile, receiver.image);
        binding.textBirthDate.setText(receiver.birthDate);
        binding.textGender.setText(receiver.gender);
        binding.textEmail.setText(receiver.email);
        binding.textName.setText(receiver.name);
        binding.textPhoneNumber.setText(receiver.phoneNumber == null ? "No data" : receiver.phoneNumber);
        setAvailability();
    }

    private void setAvailability() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(receiver.id)
                .addSnapshotListener(ReceiverInformationActivity.this, (value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                            int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                            System.out.println(availability);
                            isReceiverAvailable = (availability == Constants.AVAILABILITY);
                        }
                        receiver.token = value.getString(Constants.KEY_FCM_TOKEN);
                    }
                    if (isReceiverAvailable) {
                        binding.activeStatus.setVisibility(View.VISIBLE);
                    } else binding.activeStatus.setVisibility(View.GONE);

                });
    }
}