package com.example.facilityapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.facilityapp.R;
import com.example.facilityapp.adapters.RecentConversationsAdapter;
import com.example.facilityapp.databinding.ActivityConversationsBinding;
import com.example.facilityapp.listeners.ConversationListener;
import com.example.facilityapp.models.ChatMessage;
import com.example.facilityapp.models.User;
import com.example.facilityapp.utilities.PreferenceManager;
import com.example.facilityapp.utilities.constants;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversationsActivity extends BaseActivity implements ConversationListener {

    private ActivityConversationsBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        setListeners();
        loadUserImage();
        listenConversations();


    }

    private void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT) .show();
    }


    private void listenConversations(){
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(constants.KEY_SENDER_ID, preferenceManager.getString(constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(constants.KEY_RECEIVER_ID, preferenceManager.getString(constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    //displays a list of chat conversations in a RecyclerView,
    // and updates the list in real time based on changes made to the chat messages in Firestore.
    private final EventListener <QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversationImage = documentChange.getDocument().getString(constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(constants.KEY_RECEIVER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                    }else {
                        chatMessage.conversationImage = documentChange.getDocument().getString(constants.KEY_SENDER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(constants.KEY_SENDER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                }else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < conversations.size(); i++){
                        String senderId = documentChange.getDocument().getString(constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(constants.KEY_TIMESTAMP);
                            break;
                        }
                    }

                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };


    public void setListeners(){
        binding.ImageSettings.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        });
        binding.ImageBack.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),UsersActivity.class));
        });
    }


    public void loadUserImage(){
        byte[] bytes = Base64.decode(preferenceManager.getString(constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.ImageProfile.setImageBitmap(bitmap);
    }


    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(constants.KEY_USER, user);
        startActivity(intent);
    }

}