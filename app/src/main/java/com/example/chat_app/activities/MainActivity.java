package com.example.chat_app.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import com.example.chat_app.R;
import com.example.chat_app.adapters.RecentConversationsAdapter;
import com.example.chat_app.databinding.ActivityMainBinding;
import com.example.chat_app.fragments.listeners.ConversionListener;
import com.example.chat_app.models.ChatMessage;
import com.example.chat_app.models.User;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.example.chat_app.utilities.Render;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener, NavigationView.OnNavigationItemSelectedListener {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;
    private static int CURRENT_FRAGMENT = R.id.menuProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        loadUserDetails();
        getToken();
        setListeners();
        init();
        listenConversations();
    }

    private void init() {
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
        binding.navigationView.setItemIconTintList(null);


    }

    private void setListeners() {

        binding.addNewChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
        binding.imageProfile.setOnClickListener(v -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });
        binding.imageProfile.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));
        binding.navigationView.setNavigationItemSelectedListener(this);

    }

    private void loadUserDetails() {
        final String name = preferenceManager.getString(Constants.KEY_NAME);
        final String birthDate = preferenceManager.getString(Constants.KEY_BIRTH_DATE);
        final String gender = preferenceManager.getString(Constants.KEY_GENDER);
        binding.textName.setText(name);
        String src = preferenceManager.getString(Constants.KEY_IMAGE);
        Render.renderingBitmap(binding.imageProfile, src);

        // loading Navigation View
        View headerNav = binding.navigationView.getHeaderView(0);
        Render.renderingBitmap((RoundedImageView) headerNav.findViewById(R.id.headerImageProfile), src);
        ((TextView) headerNav.findViewById(R.id.headerTextName)).setText(name);
        ((TextView) headerNav.findViewById(R.id.headerGender)).setText(gender);
        ((TextView) headerNav.findViewById(R.id.headerTextBirtDate)).setText(birthDate);


    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            conversations.sort((obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    });

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    private void signOut() {
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }).addOnFailureListener(e -> showToast("Unable to sign out"));
    }


    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menuProfile:
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
                intent.putExtra(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                intent.putExtra(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                intent.putExtra(Constants.KEY_BIRTH_DATE, preferenceManager.getString(Constants.KEY_BIRTH_DATE));
                intent.putExtra(Constants.KEY_GENDER, preferenceManager.getString(Constants.KEY_GENDER));
                intent.putExtra(Constants.KEY_PHONE_NUMBER, preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
                startActivity(intent);
//                replaceFragment(FragmentFactory.createFragment(FragmentType.PROFILE), id);
                break;
//            case R.id.menuNotification:
//                replaceFragment(FragmentFactory.createFragment(FragmentType.NOTIFICATION), id);
//                break;
            case R.id.menuSignOut:
                signOut();
                break;
            case R.id.menuHome:
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                break;
//            case R.id.menuSetting:
//                replaceFragment(FragmentFactory.createFragment(FragmentType.SETTING), id);
//                break;
//            case R.id.menuShare:
//                replaceFragment(FragmentFactory.createFragment(FragmentType.SHARE), id);
//                break;
//        }
        }
        return true;


//    private void replaceFragment(Fragment fragment, int itemId) {
//        if (itemId == CURRENT_FRAGMENT) return;
//        CURRENT_FRAGMENT = itemId;
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.content_main, fragment);
//        fragmentTransaction.commit();
//    }

    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserDetails();
    }
}