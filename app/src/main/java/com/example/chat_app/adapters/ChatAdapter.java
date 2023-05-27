package com.example.chat_app.adapters;

import static android.content.ContentValues.TAG;
import static com.example.chat_app.utilities.Render.renderingBitmap;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.databinding.ItemContainerReceivedMessageBinding;
import com.example.chat_app.databinding.ItemContainerSentMessageBinding;
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
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    private final Application application;

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
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        else{
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) ==  VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position), application);

        }
        else{
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage, application);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position){
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }
        else return VIEW_TYPE_RECEIVED;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;

        public SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Application application){
            if(chatMessage.type.equals(Constants.KEY_TEXT_MESS)) {
                binding.textMessage.setText(chatMessage.message);
                binding.textMessage.setText(chatMessage.message);
            }
            else if(chatMessage.type.equals(Constants.KEY_IMAGE)){
                binding.textMessage.setVisibility(View.GONE);
                binding.imageMessage.setVisibility(View.VISIBLE);
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
            }
            else if(chatMessage.type.equals(Constants.KEY_VIDEO_MESS)){
                binding.textMessage.setVisibility(View.GONE);
                binding.imageVideo.setVisibility(View.VISIBLE);
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
            }
            else if(chatMessage.type.equals(Constants.KEY_RECORD_MESS)){
                binding.textMessage.setVisibility(View.GONE);
                binding.voicePlayerView.setVisibility(View.VISIBLE);
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
            }
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receivedMessage, Application application){
            if(chatMessage.type.equals(Constants.KEY_TEXT_MESS)) {
                binding.textMessage.setText(chatMessage.message);
                binding.textMessage.setText(chatMessage.message);
            }
            else if(chatMessage.type.equals(Constants.KEY_IMAGE)){
                binding.textMessage.setVisibility(View.GONE);
                binding.imageMessage.setVisibility(View.VISIBLE);
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
            }
            else if(chatMessage.type.equals(Constants.KEY_VIDEO_MESS)){
                binding.textMessage.setVisibility(View.GONE);
                binding.imageVideo.setVisibility(View.VISIBLE);
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
            }
            else if(chatMessage.type.equals(Constants.KEY_RECORD_MESS)){
                binding.textMessage.setVisibility(View.GONE);
                binding.voicePlayerView.setVisibility(View.VISIBLE);
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
            }
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receivedMessage != null){
                binding.imageProfile.setImageBitmap(receivedMessage);
            }
        }

    }
}
