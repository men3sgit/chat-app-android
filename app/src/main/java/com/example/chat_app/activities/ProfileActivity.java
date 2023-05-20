package com.example.chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivityProfileBinding;
import com.example.chat_app.models.Gender;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.DateAdapter;
import com.example.chat_app.utilities.PreferenceManager;
import com.example.chat_app.utilities.Render;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

// add image
// update firebase

public class ProfileActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        initUI();
        setListeners();

    }

    private void initUI() {
        Intent intent = getIntent();
        Render.renderingBitmap(binding.imageProfile, intent.getStringExtra(Constants.KEY_IMAGE));
        binding.textName.setText(intent.getStringExtra(Constants.KEY_NAME));
        binding.textBirthDate.setText(intent.getStringExtra(Constants.KEY_BIRTH_DATE));
        binding.textEmail.setText(intent.getStringExtra(Constants.KEY_EMAIL));
        binding.textGender.setText(intent.getStringExtra(Constants.KEY_GENDER));
        final String phoneNumber = intent.getStringExtra(Constants.KEY_PHONE_NUMBER);
        binding.textPhoneNumber.setText(phoneNumber == null ? "No data" : phoneNumber);

    }

    private void setListeners() {
        binding.buttonEdit.setOnClickListener(view -> {
            if (binding.tableEdit.getVisibility() == View.VISIBLE) {
                return;
            }
            final String email = binding.textEmail.getText().toString();
            final String name = binding.textName.getText().toString();
            final String birthDate = binding.textBirthDate.getText().toString();
            final String gender = binding.textGender.getText().toString();
            final String phoneNumber = binding.textPhoneNumber.getText().toString();
            final String encodedImage = preferenceManager.getString(Constants.KEY_IMAGE);
            binding.editEmail.setText(email);
            binding.editName.setText(name);
            binding.buttonBirthDate.setText(birthDate);
            binding.editPhoneNumber.setText(phoneNumber);


            if (gender.equalsIgnoreCase(Gender.MALE.name())) {
                binding.radioMale.setChecked(true);
            } else {
                binding.radioFemale.setChecked(true);
            }
            binding.textAddImage.setVisibility(View.VISIBLE);
            binding.tableRoot.setVisibility(View.GONE);
            binding.tableEdit.setVisibility(View.VISIBLE);
        });


        binding.buttonUpdate.setOnClickListener(view -> {
            if (binding.tableRoot.getVisibility() == View.VISIBLE) {
                return;
            }
            final String name = binding.editName.getText().toString();
            final String birthDate = binding.buttonBirthDate.getText().toString();
            final String gender = binding.radioMale.isChecked() ? Gender.MALE.name() : Gender.FEMALE.name();
            final String phoneNumber = binding.editPhoneNumber.getText().toString();

            binding.textName.setText(name);
            binding.textBirthDate.setText(birthDate);
            binding.textGender.setText(gender);
            binding.textPhoneNumber.setText(phoneNumber);

//            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
            preferenceManager.putString(Constants.KEY_NAME, name);
            preferenceManager.putString(Constants.KEY_BIRTH_DATE, birthDate);
            preferenceManager.putString(Constants.KEY_GENDER, gender);
            preferenceManager.putString(Constants.KEY_PHONE_NUMBER, phoneNumber.equalsIgnoreCase(Constants.KEY_NO_DATA) ? null : phoneNumber);

            binding.textAddImage.setVisibility(View.GONE);
            binding.tableRoot.setVisibility(View.VISIBLE);
            binding.tableEdit.setVisibility(View.GONE);
        });

        binding.buttonBirthDate.setOnClickListener(view -> {
            onClickBirthDateListener();
        });
    }

    private void onClickBirthDateListener() {
        binding.buttonBirthDate.setOnClickListener(view -> {
                    // default date
                    String src = binding.buttonBirthDate.getText().toString();
                    DateAdapter date = new DateAdapter(src);
                    // Create a new instance of DatePickerDialog and return it
                    DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                    // Do something with the selected date
                                    String date = "" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "/"
                                            + (++month < 10 ? "0" + month : month) + "/" + year;
                                    binding.buttonBirthDate.setText(date);
                                }
                            }
                            , date.getYear(), date.getMonth(), date.getDay());

                    // Show the dialog
                    datePickerDialog.show();
                }
        );
    }

    private String encodeImage(Bitmap bitmap) {
        final int previewWidth = 150;
        final int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

