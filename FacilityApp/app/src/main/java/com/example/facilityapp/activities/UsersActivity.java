package com.example.facilityapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.facilityapp.R;
import com.example.facilityapp.adapters.UsersAdapter;
import com.example.facilityapp.databinding.ActivityUsersBinding;
import com.example.facilityapp.listeners.UserListener;
import com.example.facilityapp.models.History;
import com.example.facilityapp.models.User;
import com.example.facilityapp.utilities.PreferenceManager;
import com.example.facilityapp.utilities.constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();
        getUsers();
    }


    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserID = preferenceManager.getString(constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() !=null){
                        List <User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult ()) {
                            if (currentUserID.equals(queryDocumentSnapshot)){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();

                            users.add(user);
                        }
                        if(users.size() > 0){
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecycler.setAdapter(usersAdapter);
                            binding.usersRecycler.setVisibility(View.VISIBLE);
                        }else {
                            showErrorMessage();
                        }
                    }else{
                        showErrorMessage();
                    }

                });
    }

    private void loading(boolean isLoading){
        if (isLoading == true){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showErrorMessage(){
        binding.errorMessage.setText(String.format("%s", "No Staff found on Database"));
    }

    @Override
    public void onClickedUser(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}