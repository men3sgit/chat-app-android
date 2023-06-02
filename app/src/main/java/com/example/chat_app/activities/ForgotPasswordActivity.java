package com.example.chat_app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivityForgotPasswordBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.example.chat_app.utilities.Test;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class ForgotPasswordActivity extends AppCompatActivity {
    @NonNull ActivityForgotPasswordBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }
    private void setListeners() {
        binding.backToLogin.setOnClickListener(view -> onBackPressed());
        binding.buttonCheck.setOnClickListener(view -> {
            if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString().trim()).matches()) {
                showToast("Enter valid email");
            } else {
                loadingCheck(true);
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                        .get()
                        .addOnCompleteListener(task -> {
                            //Email đã tồn tại trên hệ thống firebase
                            if (task.isSuccessful() && task.getResult() != null
                                    && task.getResult().getDocumentChanges().size() > 0) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                String id = documentSnapshot.getId();
                                showToast("Code sent to your email!!!");
                                loadingCheck(false);
                                binding.textCode.setVisibility(View.VISIBLE);
                                binding.codeEmail.setVisibility(View.VISIBLE);
                                binding.doneButton.setVisibility(View.VISIBLE);
                                binding.inputEmail.setEnabled(false);
                                binding.buttonCheck.setClickable(false);
                                String code = Test.generateRandomNumberString();
                                setCodeEmail(binding.inputEmail.getText().toString(), code);
                                database.collection(Constants.KEY_COLLECTION_USERS).document(id).update(Constants.KEY_CODE, code);
                            } else {
                                showToast("Email does not exist in the system");
                                loadingCheck(false);
                            }
                        });
            }
        });
        binding.buttonDone.setOnClickListener(v->{
            String code1 = binding.etCode1.getText().toString().trim();
            String code2 = binding.etCode2.getText().toString().trim();
            String code3 = binding.etCode3.getText().toString().trim();
            String code4 = binding.etCode4.getText().toString().trim();
            String codeEmail = code1+code2+code3+code4;
            loadingDone(true);
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                    .whereEqualTo(Constants.KEY_CODE, codeEmail).get()
                    .addOnCompleteListener(task -> {
                        //Email đã tồn tại trên hệ thống firebase
                        if (task.isSuccessful() && task.getResult() != null
                                && task.getResult().getDocumentChanges().size() > 0) {
                            Intent intent = new Intent(getApplicationContext(), UpdatePasswordActivity.class);
                            intent.putExtra("email", binding.inputEmail.getText().toString());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            showToast("Wrong code!!!");
                            loadingDone(false);
                        }
                    });
        });
    }
    private void setCodeEmail(String emailName, String code){
        try {
            String stringSenderEmail = "nguyendaccuong2002@gmail.com";
            String stringPassword = "cdofigifrlnxrbjp";
            String stringHost = "smtp.gmail.com";
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(stringSenderEmail, stringPassword);
                }
            });
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(emailName));
            mimeMessage.setSubject("TikTalk: Gui ma code cho ban!!!");
            mimeMessage.setText("Ma code cua ban la: " + code);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();

        }catch (AddressException e){
            e.printStackTrace();
        }catch (MessagingException e){
            e.printStackTrace();
        }

    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void loadingCheck(Boolean isLoading) {
        if (isLoading) {
            binding.buttonCheck.setVisibility(View.INVISIBLE);
            binding.progressBarCheck.setVisibility(View.VISIBLE);
        } else {
            binding.progressBarCheck.setVisibility(View.INVISIBLE);
            binding.buttonCheck.setVisibility(View.VISIBLE);
        }
    }
    private void loadingDone(Boolean isLoading) {
        if (isLoading) {
            binding.buttonDone.setVisibility(View.INVISIBLE);
            binding.progressBarDone.setVisibility(View.VISIBLE);
        } else {
            binding.progressBarDone.setVisibility(View.INVISIBLE);
            binding.buttonDone.setVisibility(View.VISIBLE);
        }
    }
}