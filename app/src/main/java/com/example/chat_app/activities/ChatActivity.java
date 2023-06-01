package com.example.chat_app.activities;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.devlomi.record_view.OnRecordListener;
import com.example.chat_app.R;
import com.example.chat_app.adapters.ChatAdapter;
import com.example.chat_app.databinding.ActivityChatBinding;
import com.example.chat_app.databinding.SelectSendExtendBinding;
import com.example.chat_app.models.ChatMessage;
import com.example.chat_app.models.User;
import com.example.chat_app.network.ApiClient;
import com.example.chat_app.network.ApiService;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;


public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private FirebaseStorage databaseStore;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;
    private static final int IMAGE_PICKER_REQUEST = 1;
    private MediaRecorder mediaRecorder;
    private String audioPath;
    private Boolean smooth=false;
   private ZegoSendCallInvitationButton voiceCallBtn, videoCallBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        init();
        setListeners();
        listenMessage();
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(getBitmapFromEncodedString(receiverUser.image),chatMessages, preferenceManager.getString(Constants.KEY_USER_ID), getApplication());
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();

        databaseStore = FirebaseStorage.getInstance();
        voiceCallBtn = findViewById(R.id.imageCall);
        videoCallBtn = findViewById(R.id.imageVideoCall);

        database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                receiverUser.birthDate = document.getString(Constants.KEY_BIRTH_DATE);
                receiverUser.gender = document.getString(Constants.KEY_GENDER);
                receiverUser.email = document.getString(Constants.KEY_EMAIL);
                receiverUser.phoneNumber = document.getString(Constants.KEY_PHONE_NUMBER);
                setVoiceCall();
                setVideoCall();
            }
            else{
                showToast("Error");
            }
        });
    }

    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_TYPE, Constants.KEY_TEXT_MESS);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString().trim());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId!=null){
            updateConversion(binding.inputMessage.getText().toString());
        }
        else{
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString().trim());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if(!isReceiverAvailable){
            configNotification(binding.inputMessage.getText().toString());
        }
        binding.inputMessage.setText(null);
    }
    private void configNotification(String mess){
        try{
            JSONArray tokens = new JSONArray();
            tokens.put(receiverUser.token);
            JSONObject data = new JSONObject();
            data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
            data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
            data.put(Constants.KEY_MESSAGE, mess);

            JSONObject body = new JSONObject();
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            sendNotification(body.toString());
        }
        catch (Exception e){
            showToast("Error o chatactivity: "+ e.getMessage());
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(Constants.getRemoteMsgHeaders(), messageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(response.isSuccessful()){
                            try {
                                if(response.body() != null){
                                    JSONObject responseJSon = new JSONObject(response.body());
                                    JSONArray results = responseJSon.getJSONArray("results");
                                    if(responseJSon.getInt("failure") == 1){
                                        JSONObject error = (JSONObject) results.get(0);
                                        showToast(error.getString("error"));
                                        return;
                                    }
                                }
                            } catch(JSONException e){
                                e.printStackTrace();
                            }
                            showToast("Notification sent successfully");
                        }
                        else{
                            showToast("Error: sau if "+response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        showToast(t.getMessage()+ "fail o duoi Failure");
                    }
                });
    }

    private void listenerAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
                    if(error != null) {
                        return;
                    }
                    if(value != null){
                        if(value.getLong(Constants.KEY_AVAILABILITY) != null){
                            int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                            isReceiverAvailable = (availability == Constants.AVAILABILITY);
                        }
                        receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                        setVideoCall();
                        setVoiceCall();
                        if(receiverUser.image != null){
                            receiverUser.image = value.getString(Constants.KEY_IMAGE);
                            chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                            chatAdapter.notifyItemChanged(0, chatMessages.size());
                        }
                    }
                    if(isReceiverAvailable){
                        binding.textAvailability.setVisibility(View.VISIBLE);
                    }
                    else binding.textAvailability.setVisibility(View.INVISIBLE);

                });
    }

    private void listenMessage(){
        database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null) return;
        if(value != null){
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.type = documentChange.getDocument().getString(Constants.KEY_TYPE);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            chatMessages.sort(Comparator.comparing(obj -> obj.dateObject));
            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }
            else if(smooth){
                smooth = false;
            }
            else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversionId == null){
            checkForConversion();
        }
    };
    private Bitmap getBitmapFromEncodedString(String encodedImage){
        if(encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        else return null;
    }

    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);

    }
    @SuppressLint("ClickableViewAccessibility")
    private void setListeners(){
        binding.imageBack.setOnClickListener(view -> onBackPressed());

        //ẩn bàn phím khi ấn ngoài inputmessage
        binding.chatRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Ẩn bàn phím ảo
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.inputMessage.getWindowToken(), 0);

                // Mất focus của EditText
                binding.inputMessage.clearFocus();

                return false;
            }
        });
        binding.inputMessage.setOnClickListener(v -> {
            binding.layoutSend.setVisibility(View.VISIBLE);
            binding.recordButton.setVisibility(View.INVISIBLE);
        });
        binding.inputMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused) {
                    binding.layoutSend.setVisibility(View.VISIBLE);
                    binding.recordButton.setVisibility(View.INVISIBLE);
                } else {
                    // Xử lý khi EditText mất focus
                }
            }
        });
        //gửi tin nhắn bằng ấn done từ bàn phím
        binding.inputMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Xử lý sự kiện khi người dùng nhấn nút Done trên bàn phím ảo.
                    sendMessage();
                    return true;
                }
                return false;
            }
        });

        binding.layoutSend.setOnClickListener(view -> {
            if(!binding.inputMessage.getText().toString().isEmpty()) {
                sendMessage();
            }
        });
        binding.layoutMore.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ChatActivity.this);
            SelectSendExtendBinding selectSendExtendBinding = SelectSendExtendBinding.inflate(getLayoutInflater());
            bottomSheetDialog.setContentView(selectSendExtendBinding.getRoot());
            bottomSheetDialog.show();
            selectSendExtendBinding.sendImage.setOnClickListener(view -> {
                requestPermissionImages();
                bottomSheetDialog.dismiss();
            });
            selectSendExtendBinding.sendVideo.setOnClickListener(view -> {
                requestPermissionVideo();
                bottomSheetDialog.dismiss();
            });
            selectSendExtendBinding.Record.setOnClickListener(view -> {
                //Record
                requestPermissionRecord();
                bottomSheetDialog.dismiss();
                binding.layoutSend.setVisibility(View.INVISIBLE);
                binding.recordButton.setVisibility(View.VISIBLE);
            });
        });

        binding.imageInfo.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ReceiverInformationActivity.class);
            intent.putExtra(Constants.KEY_USER, receiverUser);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        chatAdapter.setOnItemClickListener(new ChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Dialog dialog = new Dialog(ChatActivity.this);
                dialog.setContentView(R.layout.custom_dialog_image);
                ImageView imageView = dialog.findViewById(R.id.image);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storageRef.child(chatMessages.get(position).message);
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
                dialog.show();
            }
        });
        //Sự kiện đè tin nhắn để thu hồi tin nhắn
        chatAdapter.setOnItemLongClickListener(new ChatAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setMessage("Are you sure you want to unsent this message?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Thực hiện thao tác thu hồi tin nhắn ở đây
                                //Với người nhắn
                                database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                                        .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                                        .whereEqualTo(Constants.KEY_TIMESTAMP, chatMessages.get(position).dateObject)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        //Sửa thông tin trên firebase, sửa type thành text và thông tin là đã thu hồi tin nhắn
                                                        String docId = document.getId();
                                                        database.collection(Constants.KEY_COLLECTION_CHAT).document(docId).update(Constants.KEY_TYPE, Constants.KEY_RECALL);
                                                        database.collection(Constants.KEY_COLLECTION_CHAT).document(docId).update(Constants.KEY_MESSAGE, "You unsent a message");
                                                        showToast("Message unsent successful");
                                                    }
                                                } else {
                                                    showToast("Message unsent failed");
                                                }
                                            }
                                        });
                                //Với người nhận
                                database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                                        .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                                        .whereEqualTo(Constants.KEY_TIMESTAMP, chatMessages.get(position).dateObject)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        //Sửa thông tin trên firebase, sửa type thành text và thông tin là đã thu hồi tin nhắn
                                                        String docId = document.getId();
                                                        database.collection(Constants.KEY_COLLECTION_CHAT).document(docId).update(Constants.KEY_TYPE, Constants.KEY_TEXT_MESS);
                                                        database.collection(Constants.KEY_COLLECTION_CHAT).document(docId).update(Constants.KEY_MESSAGE, "You unsent a meessage");
                                                    }
                                                } else {
                                                }
                                            }
                                        });
                                smooth=true;
                                chatMessages.get(position).type = Constants.KEY_RECALL;
                                chatAdapter.notifyItemChanged(position);
                                binding.chatRecyclerView.smoothScrollToPosition(position);
                                if(!isReceiverAvailable){
                                    configNotification("You unsent a message");
                                }
                            }
                        }
                        )
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                //Hiển thị ngoài hộp thoại chat
                updateConversion("*Message has been unsent");
            }
        });
        //Sự kiện ấn voiceCall
        voiceCallBtn.setOnClickListener(v->{
            HashMap<String, Object> message = new HashMap<>();
            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            message.put(Constants.KEY_TYPE, Constants.KEY_RECALL);
            message.put(Constants.KEY_MESSAGE, "*AudioCall");
            message.put(Constants.KEY_TIMESTAMP, new Date());
            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
            if(conversionId!=null){
                updateConversion("*AudioCall");
            }
            else{
                HashMap<String, Object> conversion = new HashMap<>();
                conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                conversion.put(Constants.KEY_LAST_MESSAGE, "*AudioCall");
                conversion.put(Constants.KEY_TIMESTAMP, new Date());
                addConversion(conversion);
            }
            if(!isReceiverAvailable){
                configNotification("Audio Call");
            }
        });
        //Sự kiện ấn videoCall
        videoCallBtn.setOnClickListener(v->{
            HashMap<String, Object> message = new HashMap<>();
            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            message.put(Constants.KEY_TYPE, Constants.KEY_RECALL);
            message.put(Constants.KEY_MESSAGE, "*VideoCall");
            message.put(Constants.KEY_TIMESTAMP, new Date());
            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
            if(conversionId!=null){
                updateConversion("*VideoCall");
            }
            else{
                HashMap<String, Object> conversion = new HashMap<>();
                conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                conversion.put(Constants.KEY_LAST_MESSAGE, "*VideoCall");
                conversion.put(Constants.KEY_TIMESTAMP, new Date());
                addConversion(conversion);
            }
            if(!isReceiverAvailable){
                configNotification("Video Call");
            }
        });
    }



    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document(conversionId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
    }

    private void checkForConversion(){
        if(chatMessages.size() !=0){
            checkForConversionRemotely(preferenceManager.getString(Constants.KEY_USER_ID), receiverUser.id);
            checkForConversionRemotely(receiverUser.id, preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() >0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenerAvailabilityOfReceiver();
    }

    private void setVoiceCall(){
       voiceCallBtn.setIsVideoCall(false);
       voiceCallBtn.setResourceID("zego_uikit_call");
       voiceCallBtn.setTimeout(30);
       voiceCallBtn.setInvitees(Collections.singletonList(new ZegoUIKitUser(receiverUser.email, receiverUser.name)));
    }
    private void setVideoCall(){
       videoCallBtn.setIsVideoCall(true);
       videoCallBtn.setResourceID("zego_uikit_call");
       voiceCallBtn.setTimeout(30);
       videoCallBtn.setInvitees(Collections.singletonList(new ZegoUIKitUser(receiverUser.email, receiverUser.name)));
    }


    //image
    private void requestPermissionImages(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            openBottomPickerImages();
                        } else {
                            Toast.makeText(ChatActivity.this, "Bạn chưa cấp quyền truy cập.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }

    private void openBottomPickerImages(){
        List<Uri> selectedUriList = new ArrayList<>();
        TedBottomPicker.with(ChatActivity.this)
                .setPeekHeight(800)
                .showTitle(false)
                .setSelectMinCount(1)
                .setSelectMinCountErrorText("Please choose a photo to send")
                .setSelectMaxCount(9)
                .setSelectMaxCountErrorText("select up to 9 photos")
                .setCompleteButtonText("Send")
                .setEmptySelectionText("No Select")
                .setSelectedUriList(selectedUriList)
                .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
                    @Override
                    public void onImagesSelected(List<Uri> uriList) {
                        if(uriList!=null && !uriList.isEmpty()) {
                            sendImage(uriList);
                        }
                    }
                });
    }
    private String getNameFile(Uri uri){
        String path = uri.getPath();
        int index = path.lastIndexOf('/');
        return path.substring(index + 1);
    }
    private void sendImage(List<Uri> uriList) {
        for (Uri uri : uriList) {
            // Tạo tham chiếu đến vị trí lưu trữ của Firebase Storage
            StorageReference storageRef = databaseStore.getReference().child("images/" + getNameFile(uri));
            // Upload ảnh lên Firebase Storage
            storageRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Xử lý khi upload thành công
                        showToast("Send Image success!");
//                        Log.d(TAG, "Upload success: " + taskSnapshot.getMetadata().getPath());
                        HashMap<String, Object> message = new HashMap<>();
                        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                        message.put(Constants.KEY_TYPE, Constants.KEY_IMAGE);
                        message.put(Constants.KEY_MESSAGE, "images/" + getNameFile(uri));
                        message.put(Constants.KEY_TIMESTAMP, new Date());
                        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                        if(conversionId!=null){
                            updateConversion("Sent pictures");
                        }
                        else{
                            HashMap<String, Object> conversion = new HashMap<>();
                            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                            conversion.put(Constants.KEY_LAST_MESSAGE, "Sent pictures");
                            conversion.put(Constants.KEY_TIMESTAMP, new Date());
                            addConversion(conversion);
                        }
                        if(!isReceiverAvailable){
                            configNotification("Sent pictures");
                        }
                    })
                    .addOnFailureListener(exception -> {
                        showToast("Send Image Failed!!");
                    });

        }

    }
    //video
    private void requestPermissionVideo(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            openBottomPickerVideo();
                        } else {
                            Toast.makeText(ChatActivity.this, "Bạn chưa cấp quyền truy cập.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }

    private void openBottomPickerVideo(){
        List<Uri> selectedUriList = new ArrayList<>();
        TedBottomPicker.with(ChatActivity.this)
                .showVideoMedia()
                .setPeekHeight(800)
                .showTitle(false)
                .setSelectMinCount(1)
                .setSelectMinCountErrorText("Please choose a video to send")
                .setSelectMaxCount(1)
                .setSelectMaxCountErrorText("select up to 1 video")
                .setCompleteButtonText("Send")
                .setEmptySelectionText("No Select")
                .setSelectedUriList(selectedUriList)
                .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
                    @Override
                    public void onImagesSelected(List<Uri> uriList) {
                        if(uriList!=null && !uriList.isEmpty()) {
                            sendVideo(uriList);
                        }
                    }
                });
    }

    private void sendVideo(List<Uri> uris) {
        for(Uri uri : uris){
            // Tạo tham chiếu đến vị trí lưu trữ của Firebase Storage
            StorageReference storageRef = databaseStore.getReference().child("videos/" + getNameFile(uri));
            // Upload ảnh lên Firebase Storage
            storageRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Xử lý khi upload thành công
                        showToast("Send Video success!");
//                  Log.d(TAG, "Upload success: " + taskSnapshot.getMetadata().getPath());
                        HashMap<String, Object> message = new HashMap<>();
                        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                        message.put(Constants.KEY_TYPE, Constants.KEY_VIDEO_MESS);
                        message.put(Constants.KEY_MESSAGE, "videos/" + getNameFile(uri));
                        message.put(Constants.KEY_TIMESTAMP, new Date());
                        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                        if(conversionId!=null){
                            updateConversion("Sent Video");
                        }
                        else{
                            HashMap<String, Object> conversion = new HashMap<>();
                            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                            conversion.put(Constants.KEY_LAST_MESSAGE, "Sent Video");
                            conversion.put(Constants.KEY_TIMESTAMP, new Date());
                            addConversion(conversion);
                        }
                        if(!isReceiverAvailable){
                            configNotification("Sent Video");
                        }
                    })
                    .addOnFailureListener(exception -> {
                        showToast("Send Video Failed!!");
                    });
        }
    }
    private void getRecord() {
        binding.recordButton.setRecordView(binding.recordView);
        binding.recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                showToast("Start");
                setUpRecord();

                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                binding.inputMessage.setVisibility(View.INVISIBLE);
                binding.layoutMore.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                showToast("Cancel");

                mediaRecorder.reset();
                mediaRecorder.release();

                File file = new File(audioPath);
                if(!file.exists())
                    file.delete();

                binding.inputMessage.setVisibility(View.VISIBLE);
                binding.layoutMore.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                //Stop Recording..
                //limitReached to determine if the Record was finished when time limit reached.
//                showToast("Finish: "+recordTime);

                mediaRecorder.stop();
                mediaRecorder.release();

                binding.inputMessage.setVisibility(View.VISIBLE);
                binding.layoutMore.setVisibility(View.VISIBLE);
                sendRecording(audioPath);
            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                showToast("LessThanSecond");
                mediaRecorder.reset();
                mediaRecorder.release();
                File file = new File(audioPath);
                if(!file.exists())
                    file.delete();

                binding.inputMessage.setVisibility(View.VISIBLE);
                binding.layoutMore.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLock() {
                //When Lock gets activated
                Log.d("RecordView", "onLock");
            }

        });
    }

    private void setUpRecord() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        audioPath= getCacheDir().getAbsolutePath() + "/"+ UUID.randomUUID()+".mp4";
        mediaRecorder.setOutputFile(audioPath);
    }

    private void sendRecording(String audioPath) {
        Uri uri = Uri.fromFile(new File(audioPath));
        System.out.println(audioPath.toString());
        // Tạo tham chiếu đến vị trí lưu trữ của Firebase Storage
        StorageReference storageRef = databaseStore.getReference().child("records/" + getNameFile(uri));
        // Upload voice lên Firebase Storage
        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Xử lý khi upload thành công
                    showToast("Send Record success!");
//                  Log.d(TAG, "Upload success: " + taskSnapshot.getMetadata().getPath());
                    HashMap<String, Object> message = new HashMap<>();
                    message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                    message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                    message.put(Constants.KEY_TYPE, Constants.KEY_RECORD_MESS);
                    message.put(Constants.KEY_MESSAGE, "records/" + getNameFile(uri));
                    message.put(Constants.KEY_TIMESTAMP, new Date());
                    database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                    if(conversionId!=null){
                        updateConversion("Sent Record");
                    }
                    else{
                        HashMap<String, Object> conversion = new HashMap<>();
                        conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                        conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                        conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                        conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                        conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                        conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                        conversion.put(Constants.KEY_LAST_MESSAGE, "Sent Record");
                        conversion.put(Constants.KEY_TIMESTAMP, new Date());
                        addConversion(conversion);
                    }
                    if(!isReceiverAvailable){
                        configNotification("Sent Record");
                    }
                })
                .addOnFailureListener(exception -> {
                    showToast("Send Record Failed!!");
                });
    }
    private void requestPermissionRecord(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            getRecord();
                        } else {
                            Toast.makeText(ChatActivity.this, "Bạn chưa cấp quyền truy cập.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }
}