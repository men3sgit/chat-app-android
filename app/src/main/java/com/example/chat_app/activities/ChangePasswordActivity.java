package com.example.chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivityChangePasswordBinding;
import com.example.chat_app.databinding.ActivitySettingBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ChangePasswordActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    ActivityChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.buttonBack.setOnClickListener(v -> onBackPressed());

        binding.buttonUpdatePassword.setOnClickListener(view -> {
            if (!binding.editCurrentPassword.getText().toString().equals(preferenceManager.getString(Constants.KEY_PASSWORD))) {

                showToast("Current password wrong");
                return;
            }
            if (binding.editNewPassword.getText().toString().trim().length() < 6) {
                showToast("Password length must larger than 6 characters");
                return;
            }
            if (binding.editNewPassword.getText().toString().equals(binding.editConfirmNewPassword.getText().toString())) {
                final String password = binding.editConfirmNewPassword.getText().toString();
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                HashMap<String, Object> user = new HashMap<>();
                user.put(Constants.KEY_PASSWORD, password);
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID))
                        .update(user)
                        .addOnSuccessListener(documentReference -> {
                            preferenceManager.putString(Constants.KEY_PASSWORD, password);
                            showToast("Update password success!");
                            clearInputs();
                            onBackPressed();
                        }).addOnFailureListener(exception -> {
                            showToast(exception.getMessage());
                        });
            } else {
                showToast("Password no match!");
            }

        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void clearInputs() {
        binding.editCurrentPassword.setText("");
        binding.editNewPassword.setText("");
        binding.editConfirmNewPassword.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearInputs();
    }
}