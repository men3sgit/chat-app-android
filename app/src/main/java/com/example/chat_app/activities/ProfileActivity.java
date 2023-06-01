package com.example.chat_app.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivityProfileBinding;
import com.example.chat_app.models.Gender;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.DateAdapter;
import com.example.chat_app.utilities.PreferenceManager;
import com.example.chat_app.utilities.Render;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;

// add image
// update firebase

public class ProfileActivity extends BaseActivity {
    private PreferenceManager preferenceManager;
    private ActivityProfileBinding binding;
    private String encodedImage;

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
        encodedImage = intent.getStringExtra(Constants.KEY_IMAGE);
        Render.renderingBitmap(binding.imageProfile, encodedImage);
        binding.textName.setText(intent.getStringExtra(Constants.KEY_NAME));
        binding.textBirthDate.setText(intent.getStringExtra(Constants.KEY_BIRTH_DATE));
        binding.textEmail.setText(intent.getStringExtra(Constants.KEY_EMAIL));
        binding.textGender.setText(intent.getStringExtra(Constants.KEY_GENDER));
        final String phoneNumber = intent.getStringExtra(Constants.KEY_PHONE_NUMBER);
        binding.textPhoneNumber.setText(phoneNumber == null ? "No data" : phoneNumber);
    }

    private void setListeners() {
        binding.buttonBackToMain.setOnClickListener(view -> finish());
        binding.imageProfile.setOnClickListener(view -> {
            Dialog dialog = new Dialog(ProfileActivity.this);
            dialog.setContentView(R.layout.custom_dialog_image);
            ImageView imageView = dialog.findViewById(R.id.image);
            Bitmap bitmap = Render.renderingBitmap(encodedImage);
            imageView.setImageBitmap(bitmap);
            dialog.show();
        });
        binding.buttonEdit.setOnClickListener(view -> {
            if (binding.tableEdit.getVisibility() == View.VISIBLE) {
                return;
            }
//            final String encodedImage = preferenceManager.getString(Constants.KEY_IMAGE);
            final String email = binding.textEmail.getText().toString();
            final String name = binding.textName.getText().toString();
            final String birthDate = binding.textBirthDate.getText().toString();
            final String gender = binding.textGender.getText().toString();
            final String phoneNumber = binding.textPhoneNumber.getText().toString();
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
        binding.textAddImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.buttonUpdate.setOnClickListener(view -> {
            if (binding.tableRoot.getVisibility() == View.VISIBLE) {
                showToast("Nothing to update!");
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

            // firebase store
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String, Object> user = new HashMap<>();
            //user.put(Constants.KEY_IMAGE, encodedImage);
            user.put(Constants.KEY_NAME, name);
            user.put(Constants.KEY_BIRTH_DATE, birthDate);
            user.put(Constants.KEY_GENDER, gender);
            user.put(Constants.KEY_PHONE_NUMBER, phoneNumber);
            user.put(Constants.KEY_IMAGE, encodedImage);

            database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .update(user)
                    .addOnSuccessListener(documentReference -> {
                        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                        preferenceManager.putString(Constants.KEY_NAME, name);
                        preferenceManager.putString(Constants.KEY_BIRTH_DATE, birthDate);
                        preferenceManager.putString(Constants.KEY_GENDER, gender);
                        preferenceManager.putString(Constants.KEY_PHONE_NUMBER, phoneNumber.equalsIgnoreCase(Constants.KEY_NO_DATA) ? null : phoneNumber);
                        showToast("Update success!");
                    }).addOnFailureListener(exception -> {
                        showToast(exception.getMessage());
                    });

            binding.textAddImage.setVisibility(View.GONE);
            binding.tableRoot.setVisibility(View.VISIBLE);
            binding.tableEdit.setVisibility(View.GONE);


        });

        binding.buttonBirthDate.setOnClickListener(view -> {
            onClickBirthDateListener();
        });
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputSteam = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputSteam);
                            //Hiện ảnh đã chọn lên imageProfile
                            binding.imageProfile.setImageBitmap(bitmap);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Picasso.get().load(imageUri).into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // nén ảnh trước khi chuyển đổi thành chuỗi
                                byte[] byteArray = byteArrayOutputStream.toByteArray();
                                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                encodedImage = encoded;
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {}

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {}
                        });
                    }
                }
            }
    );
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
                            , date.getYear(), date.getMonth() - 1, date.getDay());

                    // Show the dialog
                    datePickerDialog.show();
                }
        );
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

