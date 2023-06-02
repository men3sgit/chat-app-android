package com.example.chat_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.chat_app.R;
import com.example.chat_app.adapters.UsersAdapter;
import com.example.chat_app.databinding.ActivityUsersBinding;
import com.example.chat_app.fragments.listeners.UserListener;
import com.example.chat_app.models.User;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        // avoid require input from user's keyboard
        binding.searchView.clearFocus();
        setListeners();
        getUsers();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        setSearchViewListener();
    }

    private void setSearchViewListener() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    showAllUsers();
                } else {
                    filterList(newText);
                }
                return false;
            }
        });

    }

    private void filterList(String newText) {
        List<User> filteredByNameList = new ArrayList<>();
        List<User> filteredByEmailList = new ArrayList<>();
        for (User user : this.users) {
            if (user.email.toLowerCase().contains(newText.toLowerCase())) // search for email
            { // search for name
                filteredByEmailList.add(user);
            }
            if (user.name.toLowerCase().contains(newText.toLowerCase())) {
                filteredByNameList.add(user);
            }
        }
        if (filteredByEmailList.isEmpty() && filteredByNameList.isEmpty()) {
            binding.textDataNotFound.setVisibility(View.VISIBLE);
            binding.layoutNameData.setVisibility(View.GONE);
            binding.usersRecyclerView.setVisibility(View.GONE);
            binding.line.setVisibility(View.GONE);
            binding.layoutEmailData.setVisibility(View.GONE);
            binding.usersByEmailRecyclerView.setVisibility(View.GONE);
        } else {
            if (!filteredByNameList.isEmpty()) {
                UsersAdapter usersAdapter = new UsersAdapter(filteredByNameList, this);
                binding.usersRecyclerView.setAdapter(usersAdapter);
                binding.line.setVisibility(View.GONE);
                binding.textTitleName.setText(R.string.name);
                binding.textQtyName.setText(String.format("(%d)", filteredByNameList.size()));
                binding.layoutNameData.setVisibility(View.VISIBLE);
                binding.usersRecyclerView.setVisibility(View.VISIBLE);
            } else {
                binding.usersRecyclerView.setVisibility(View.GONE);
                binding.layoutNameData.setVisibility(View.GONE);
                binding.line.setVisibility(View.GONE);
            }
            if (!filteredByEmailList.isEmpty()) {
                UsersAdapter usersAdapter = new UsersAdapter(filteredByEmailList, this);
                binding.usersByEmailRecyclerView.setAdapter(usersAdapter);
                binding.textTitleEmail.setText(R.string.email);
                binding.textQtyEmail.setText(String.format("(%d)", filteredByEmailList.size()));
                binding.usersByEmailRecyclerView.setVisibility(View.VISIBLE);
                binding.layoutEmailData.setVisibility(View.VISIBLE);
                if(!filteredByNameList.isEmpty()){
                    binding.line.setVisibility(View.VISIBLE);
                }
            } else {
                binding.layoutEmailData.setVisibility(View.GONE);
                binding.usersByEmailRecyclerView.setVisibility(View.GONE);
            }
            binding.textDataNotFound.setVisibility(View.GONE);
        }
    }

    public void setFilteredList(List<User> filteredList) {
        this.users = filteredList;
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(task -> {
            loading(false);
            String currentId = preferenceManager.getString(Constants.KEY_USER_ID);
            if (task.isSuccessful() && task.getResult() != null) {
                users = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    if (currentId.equals(queryDocumentSnapshot.getId())) {
                        continue;
                    }
                    User user = new User();
                    user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                    user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                    user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                    user.birthDate = queryDocumentSnapshot.getString(Constants.KEY_BIRTH_DATE);
                    user.gender = queryDocumentSnapshot.getString(Constants.KEY_GENDER);
                    user.phoneNumber = queryDocumentSnapshot.getString(Constants.KEY_PHONE_NUMBER);
                    user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                    user.id = queryDocumentSnapshot.getId();
                    users.add(user);
                }
                //MT list
                if (users.size() >= 0) {
                    showAllUsers();
                } else showErrorMessage();
            } else showErrorMessage();
        });
    }
    public void showAllUsers(){
        UsersAdapter usersAdapter = new UsersAdapter(users, this);
        binding.usersRecyclerView.setAdapter(usersAdapter);

        binding.textQtyName.setText(String.format("(%d)", users.size()));
        binding.textTitleName.setText("Suggested people");
        binding.layoutNameData.setVisibility(View.VISIBLE);
        binding.usersRecyclerView.setVisibility(View.VISIBLE);
        binding.textDataNotFound.setVisibility(View.GONE);
    }


    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else binding.progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}