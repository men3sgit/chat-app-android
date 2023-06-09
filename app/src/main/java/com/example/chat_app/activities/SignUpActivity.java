package com.example.chat_app.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.chat_app.databinding.ActivitySignUpBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.example.chat_app.utilities.ViewAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class SignUpActivity extends AppCompatActivity {
    private DatePickerDialog datePickerDialog;
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        //sự kiện trở về SignIn
        binding.textSignIn.setOnClickListener(view -> onBackPressed());
        //sự kiến ấn button
        binding.buttonSignUp.setOnClickListener(v -> {
                    //Kiểm tra hợp lệ hay không
                    if (isValidSignUpDetails())
                        signUp();
                }
        );
        //sự kiện chọn hình ảnh
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });
        //sử kiện chọn ngày sinh nhật
        setButtonBirthDateListener();
        //sự kiển hiện mật khẩu
        setShowPasswordListener();
        //sự kiện ẩn mật khẩu
        setHidePasswordListener();

    }

    private void setHidePasswordListener() {
        // show confirm password
        binding.textShowConfirmPassword.setOnClickListener(view -> {
            binding.inputConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.textShowConfirmPassword.setVisibility(View.GONE);
            binding.textHideConfirmPassword.setVisibility(View.VISIBLE);
            ViewAdapter.setEndOfText(binding.inputConfirmPassword);
        });

        // hide confirm password
        binding.textHideConfirmPassword.setOnClickListener(view -> {
            binding.inputConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.textHideConfirmPassword.setVisibility(View.GONE);
            binding.textShowConfirmPassword.setVisibility(View.VISIBLE);
            ViewAdapter.setEndOfText(binding.inputConfirmPassword);
        });
    }

    private void setShowPasswordListener() {
        // show password
        binding.textShowPassword.setOnClickListener(view -> {
            binding.inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.textShowPassword.setVisibility(View.GONE);
            binding.textHidePassword.setVisibility(View.VISIBLE);
            ViewAdapter.setEndOfText(binding.inputPassword);
        });

        // hide password
        binding.textHidePassword.setOnClickListener(view -> {
            binding.inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.textHidePassword.setVisibility(View.GONE);
            binding.textShowPassword.setVisibility(View.VISIBLE);
            ViewAdapter.setEndOfText(binding.inputPassword);
        });

    }

    private void setButtonBirthDateListener() {
        binding.buttonBirthDate.setOnClickListener(view -> {
                    final Calendar calendar = Calendar.getInstance();
                    // default date
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                    // Create a new instance of DatePickerDialog and return it
                    datePickerDialog = new DatePickerDialog(SignUpActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                    // Do something with the selected date
                                    String date = "" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "/"
                                            + (++month < 10 ? "0" + month : month) + "/" + year;
                                    binding.buttonBirthDate.setText(date);
                                }
                            }
                            , year, month, dayOfMonth);

                    // Show the dialog
                    datePickerDialog.show();
                }
        );
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    // 7 attributes
    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    //Email đã tồn tại trên hệ thống firebase
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocumentChanges().size() > 0) {
                        showToast("Email Exists");
                        loading(false);
                    }
                    else{
                        HashMap<String, Object> user = new HashMap<>();
                        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
                        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
                        user.put(Constants.KEY_IMAGE, encodedImage);
                        user.put(Constants.KEY_BIRTH_DATE, binding.buttonBirthDate.getText().toString());
                        user.put(Constants.KEY_GENDER, getGender());
                        user.put(Constants.KEY_PHONE_NUMBER,null);
                        database.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(documentReference -> {
                            loading(false);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                            preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                            preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                            preferenceManager.putString(Constants.KEY_BIRTH_DATE, binding.buttonBirthDate.getText().toString());
                            preferenceManager.putString(Constants.KEY_GENDER, getGender());
                            preferenceManager.putString(Constants.KEY_PHONE_NUMBER, null);

                            preferenceManager.putBoolean("NIGHT_MODE", false);

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }).addOnFailureListener(exception -> {
                            loading(false);
                            showToast(exception.getMessage());
                        });
                    }
                });
    }

    private String getGender() {
        if (binding.radioMale.isChecked()) return binding.radioMale.getText().toString();
        return binding.radioFemale.getText().toString();
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
                            //Ẩn text AddImage
                            binding.textAddImage.setVisibility(View.GONE);

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

    private Boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast("Select profile image");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString().trim()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().length() < 6) {
            showToast("Password length must larger than 6 characters");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().length() < 6) {
            showToast("Confirm password length must larger than 6 characters");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password and confirm password must be same");
            return false;
        } else if (binding.buttonBirthDate.getText().toString().equalsIgnoreCase("birth date")) {
            showToast("Enter Birth Date");
            return false;
        }
        return true;
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}