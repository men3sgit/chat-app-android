package com.example.chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chat_app.databinding.ActivityUpdatePasswordBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.example.chat_app.utilities.Test;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdatePasswordActivity extends AppCompatActivity {
    private ActivityUpdatePasswordBinding binding;
    private PreferenceManager preferenceManager;
    private String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdatePasswordBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        email = (String) getIntent().getSerializableExtra("email");
        binding.emailText.setText("Change password for account: "+email);
        setListeners();
    }
    private void setListeners(){
        binding.buttonUpdate.setOnClickListener(v -> {
            if(binding.editNewPassword.getText().toString().trim().length() < 6){
                showToast("Password length must larger than 6 characters");
            }
            else if(!binding.editNewPassword.getText().toString().equals(binding.editConfirmNewPassword.getText().toString())){
                showToast("Password and confirm password must be same");
            }
            else{
                loading(true);
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL, email)
                        .get()
                        .addOnCompleteListener(task -> {
                            //Email đã tồn tại trên hệ thống firebase
                            if (task.isSuccessful() && task.getResult() != null
                                    && task.getResult().getDocumentChanges().size() > 0) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                String id = documentSnapshot.getId();
                                showToast("Code sent to your email!!!");
                                database.collection(Constants.KEY_COLLECTION_USERS).document(id).update(Constants.KEY_PASSWORD, binding.editNewPassword.getText().toString());
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                                preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                                preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                preferenceManager.putString(Constants.KEY_PASSWORD, binding.editNewPassword.getText().toString());
                                preferenceManager.putString(Constants.KEY_BIRTH_DATE, documentSnapshot.getString(Constants.KEY_BIRTH_DATE));
                                preferenceManager.putString(Constants.KEY_GENDER, documentSnapshot.getString(Constants.KEY_GENDER));
                                preferenceManager.putBoolean("NIGHT_MODE", false);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                showToast("Error");
                                loading(false);
                            }
                        });
            }
        });
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    public void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonUpdate.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonUpdate.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}