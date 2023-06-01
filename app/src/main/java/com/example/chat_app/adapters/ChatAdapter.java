package com.example.chat_app.adapters;

import static android.content.ContentValues.TAG;
import static com.example.chat_app.utilities.Render.renderingBitmap;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.databinding.ItemContainerReceivedImageBinding;
import com.example.chat_app.databinding.ItemContainerReceivedMessageBinding;
import com.example.chat_app.databinding.ItemContainerReceivedRecallBinding;
import com.example.chat_app.databinding.ItemContainerReceivedRecordBinding;
import com.example.chat_app.databinding.ItemContainerReceivedVideoBinding;
import com.example.chat_app.databinding.ItemContainerSentImageBinding;
import com.example.chat_app.databinding.ItemContainerSentMessageBinding;
import com.example.chat_app.databinding.ItemContainerSentRecallBinding;
import com.example.chat_app.databinding.ItemContainerSentRecordBinding;
import com.example.chat_app.databinding.ItemContainerSentVideoBinding;
import com.example.chat_app.models.ChatMessage;
import com.example.chat_app.utilities.Constants;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Bitmap receiverProfileImage;
    private final List<ChatMessage> chatMessages;
    private final String senderId;
    public static final int VIEW_TYPE_SENT_TEXT = 1;
    public static final int VIEW_TYPE_RECEIVED_TEXT = 2;
    public static final int VIEW_TYPE_SENT_IMAGE = 3;
    public static final int VIEW_TYPE_RECEIVED_IMAGE = 4;
    public static final int VIEW_TYPE_SENT_VIDEO = 5;
    public static final int VIEW_TYPE_RECEIVED_VIDEO = 6;
    public static final int VIEW_TYPE_SENT_RECORD = 7;
    public static final int VIEW_TYPE_RECEIVED_RECORD = 8;
    public static final int VIEW_TYPE_SENT_RECALL = 9;
    public static final int VIEW_TYPE_RECEIVED_RECALL = 10;
    private final Application application;
    private OnItemLongClickListener onItemLongClickListener;
    public void setOnItemLongClickListener(OnItemLongClickListener onItem){
        this.onItemLongClickListener = onItem;
    }

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(Bitmap receiverProfileImage, List<ChatMessage> chatMessages, String senderId, Application application) {
        this.receiverProfileImage = receiverProfileImage;
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.application = application;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT_TEXT){
            return new SentMessageViewHolder(ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        else if(viewType == VIEW_TYPE_SENT_IMAGE){
            return new SentImageViewHolder(ItemContainerSentImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        else if(viewType == VIEW_TYPE_SENT_VIDEO){
            return new SentVideoViewHolder(ItemContainerSentVideoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        else if(viewType == VIEW_TYPE_SENT_RECORD){
            return new SentRecordViewHolder(ItemContainerSentRecordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        else if(viewType == VIEW_TYPE_SENT_RECALL){
            return new SentRecallViewHolder(ItemContainerSentRecallBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        else if(viewType == VIEW_TYPE_RECEIVED_TEXT){
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false));
        }
        else if(viewType == VIEW_TYPE_RECEIVED_IMAGE){
            return new ReceivedImageViewHolder(ItemContainerReceivedImageBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false));
        }
        else if(viewType == VIEW_TYPE_RECEIVED_VIDEO){
            return new ReceivedVideoViewHolder(ItemContainerReceivedVideoBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false));
        }
        else if(viewType == VIEW_TYPE_RECEIVED_RECORD){
            return new ReceivedRecordViewHolder(ItemContainerReceivedRecordBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false));
        }
        else if(viewType == VIEW_TYPE_RECEIVED_RECALL){
            return new ReceivedRecallViewHolder(ItemContainerReceivedRecallBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if(getItemViewType(position) ==  VIEW_TYPE_SENT_TEXT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position), application);

            ((SentMessageViewHolder) holder).binding.textMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(v, position);
                    }
                    return true;
                }
            });
        }
        else if(getItemViewType(position) ==  VIEW_TYPE_SENT_IMAGE){
            ((SentImageViewHolder) holder).setData(chatMessages.get(position), application);

            ((SentImageViewHolder) holder).binding.imageMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(v, position);
                    }
                    return true;
                }
            });
        }
        else if(getItemViewType(position) ==  VIEW_TYPE_SENT_VIDEO){
            ((SentVideoViewHolder) holder).setData(chatMessages.get(position), application);
            ((SentVideoViewHolder) holder).binding.imageVideo.setOnTouchListener(new View.OnTouchListener() {
                private Handler mHandler;
                private Runnable mRunnable;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mHandler = new Handler();
                            mRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    // Xử lý sự kiện long press ở đây
                                    onItemLongClickListener.onItemLongClick(v, position);
                                }
                            };
                            mHandler.postDelayed(mRunnable, 500); // Thời gian để xác định long press (tính bằng milliseconds)
                            break;
                    }
                    return true;
                }
            });
        }
        else if(getItemViewType(position) ==  VIEW_TYPE_SENT_RECORD){
            ((SentRecordViewHolder) holder).setData(chatMessages.get(position), application);
            ((SentRecordViewHolder) holder).binding.voicePlayerView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(v, position);
                    }
                    return true;
                }
            });
        }
        else if(getItemViewType(position) ==  VIEW_TYPE_SENT_RECALL){
            ((SentRecallViewHolder) holder).setData(chatMessages.get(position), application);
        }
        else if(getItemViewType(position) ==  VIEW_TYPE_RECEIVED_TEXT){
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage, application);
        }
        else if(getItemViewType(position) ==  VIEW_TYPE_RECEIVED_IMAGE){
            ((ReceivedImageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage, application);
        }
        else if(getItemViewType(position) ==  VIEW_TYPE_RECEIVED_VIDEO){
            ((ReceivedVideoViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage, application);
        }
        else if(getItemViewType(position) ==  VIEW_TYPE_RECEIVED_RECORD){
            ((ReceivedRecordViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage, application);
        }
        else if(getItemViewType(position) ==  VIEW_TYPE_RECEIVED_RECALL){
            ((ReceivedRecallViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage, application);
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position){
        //Gui tin nhan text
        if(chatMessages.get(position).senderId.equals(senderId) && chatMessages.get(position).type.equals(Constants.KEY_TEXT_MESS)){
            return VIEW_TYPE_SENT_TEXT;
        }
        //gui tin nhan hinh anh image
        else if(chatMessages.get(position).senderId.equals(senderId) && chatMessages.get(position).type.equals(Constants.KEY_IMAGE)){
            return VIEW_TYPE_SENT_IMAGE;
        }
        //gui tin nhan video
        else if(chatMessages.get(position).senderId.equals(senderId) && chatMessages.get(position).type.equals(Constants.KEY_VIDEO_MESS)){
            return VIEW_TYPE_SENT_VIDEO;
        }
        //thu hoi tin nhan da gui
        else if(chatMessages.get(position).senderId.equals(senderId) && chatMessages.get(position).type.equals(Constants.KEY_RECALL)){
            return VIEW_TYPE_SENT_RECALL;
        }
        //gui tin nhan ghi am
        else if(chatMessages.get(position).senderId.equals(senderId) && chatMessages.get(position).type.equals(Constants.KEY_RECORD_MESS)){
            return VIEW_TYPE_SENT_RECORD;
        }
        //nhan tin nhan text
        else if(chatMessages.get(position).receiverId.equals(senderId) && chatMessages.get(position).type.equals(Constants.KEY_TEXT_MESS)){
            return VIEW_TYPE_RECEIVED_TEXT;
        }
        //nhan tin nhan image
        else if(chatMessages.get(position).receiverId.equals(senderId) && chatMessages.get(position).type.equals(Constants.KEY_IMAGE)){
            return VIEW_TYPE_RECEIVED_IMAGE;
        }
        //nhan tin nhan video
        else if(chatMessages.get(position).receiverId.equals(senderId) && chatMessages.get(position).type.equals(Constants.KEY_VIDEO_MESS)){
            return VIEW_TYPE_RECEIVED_VIDEO;
        }
        //nhan tin nhan ghi am
        else if(chatMessages.get(position).receiverId.equals(senderId) && chatMessages.get(position).type.equals(Constants.KEY_RECORD_MESS)){
            return VIEW_TYPE_RECEIVED_RECORD;
        }
        //thu hoi tin nhan ben nguoi nhan
        else if(chatMessages.get(position).receiverId.equals(senderId) && chatMessages.get(position).type.equals(Constants.KEY_RECALL)){
            return VIEW_TYPE_RECEIVED_RECALL;
        }
        return 0;
    }
    //Class Sent Image
    static class SentImageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentImageBinding binding;

        public SentImageViewHolder(ItemContainerSentImageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Application application){
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storageRef.child(chatMessage.message);
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(binding.imageMessage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            binding.textDateTime.setText(chatMessage.dateTime);
            }
    }
    //class sent video
    static class SentVideoViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentVideoBinding binding;

        public SentVideoViewHolder(ItemContainerSentVideoBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Application application){
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storageRef.child(chatMessage.message);
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String fullVideoUrl = uri.toString();
                        Log.d(TAG, "Full video url: " + fullVideoUrl);
                        // Load video into ExoPlayer.
                        SimpleExoPlayer exoPlayer;
                        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(application).build();
                        TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                        exoPlayer = (SimpleExoPlayer) ExoPlayerFactory.newSimpleInstance(application, trackSelector);
                        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("video");
                        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(fullVideoUrl), dataSourceFactory, extractorsFactory, null, null);
                        binding.imageVideo.setPlayer(exoPlayer);
                        exoPlayer.prepare(mediaSource);
                        exoPlayer.setPlayWhenReady(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "Unable to get download URL for video.", exception);
                    }
                });
            binding.textDateTime.setText(chatMessage.dateTime);
            }
    }
    //sent record
    static class SentRecordViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentRecordBinding binding;

        public SentRecordViewHolder(ItemContainerSentRecordBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Application application){
                VoicePlayerView voicePlayerView;
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference recordRef = storageRef.child(chatMessage.message);
                recordRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        binding.voicePlayerView.setAudio(uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }
    static class SentRecallViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentRecallBinding binding;

        public SentRecallViewHolder(ItemContainerSentRecallBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Application application){
            binding.textRecall.setText("*Message has been withdrawn");
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }
    //sent message text
    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;

        public SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Application application){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }
    //received image
    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedImageBinding binding;

        public ReceivedImageViewHolder(ItemContainerReceivedImageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receivedMessage, Application application){
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storageRef.child(chatMessage.message);
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(binding.imageMessage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receivedMessage != null){
                binding.imageProfile.setImageBitmap(receivedMessage);
            }
        }
    }
    //received video
    static class ReceivedVideoViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedVideoBinding binding;

        public ReceivedVideoViewHolder(ItemContainerReceivedVideoBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receivedMessage, Application application){
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storageRef.child(chatMessage.message);
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String fullVideoUrl = uri.toString();
//                        Log.d(TAG, "Full video url: " + fullVideoUrl);
                        // Load video into ExoPlayer.
                        SimpleExoPlayer exoPlayer;
                        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(application).build();
                        TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                        exoPlayer = (SimpleExoPlayer) ExoPlayerFactory.newSimpleInstance(application, trackSelector);
                        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("video");
                        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(fullVideoUrl), dataSourceFactory, extractorsFactory, null, null);
                        binding.imageVideo.setPlayer(exoPlayer);
                        exoPlayer.prepare(mediaSource);
                        exoPlayer.setPlayWhenReady(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "Unable to get download URL for video.", exception);
                    }
                });
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receivedMessage != null){
                binding.imageProfile.setImageBitmap(receivedMessage);
            }
        }
    }
    //class received record
    static class ReceivedRecordViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedRecordBinding binding;

        public ReceivedRecordViewHolder(ItemContainerReceivedRecordBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receivedMessage, Application application){
                VoicePlayerView voicePlayerView;
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference recordRef = storageRef.child(chatMessage.message);
                recordRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        binding.voicePlayerView.setAudio(uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receivedMessage != null){
                binding.imageProfile.setImageBitmap(receivedMessage);
            }
        }

    }
    //class recall
    static class ReceivedRecallViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedRecallBinding binding;

        public ReceivedRecallViewHolder(ItemContainerReceivedRecallBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receivedMessage, Application application){
            binding.textRecall.setText("*Message has been withdrawn");
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receivedMessage != null){
                binding.imageProfile.setImageBitmap(receivedMessage);
            }
        }

    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receivedMessage, Application application){
                binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receivedMessage != null){
                binding.imageProfile.setImageBitmap(receivedMessage);
            }
        }

    }
    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }
}
