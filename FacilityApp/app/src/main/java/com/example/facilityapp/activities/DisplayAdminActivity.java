package com.example.facilityapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.facilityapp.R;
import com.example.facilityapp.adapters.AdminAdapter;
import com.example.facilityapp.adapters.UsersAdapter;
import com.example.facilityapp.databinding.ActivityDisplayAdminBinding;
import com.example.facilityapp.databinding.ActivityUsersBinding;
import com.example.facilityapp.listeners.UserListener;
import com.example.facilityapp.models.User;
import com.example.facilityapp.utilities.PreferenceManager;
import com.example.facilityapp.utilities.constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DisplayAdminActivity extends AppCompatActivity{

    private ActivityDisplayAdminBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDisplayAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();
        getAdmin();


    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getAdmin(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(constants.KEY_COLLECTION_FACILITY)
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
                            User admin = new User();
                            admin.name = queryDocumentSnapshot.getString(constants.KEY_NAME);
                            admin.email = queryDocumentSnapshot.getString(constants.KEY_EMAIL);
                            admin.image = queryDocumentSnapshot.getString(constants.KEY_IMAGE);
                            admin.token = queryDocumentSnapshot.getString(constants.KEY_FCM_TOKEN);
                            admin.id = queryDocumentSnapshot.getId();

                            if (queryDocumentSnapshot.getId().equals(currentUserID)){
                                admin.name = "You";
                            }

                            users.add(admin);
                        }
                        if(users.size() > 0){
                            AdminAdapter adminAdapter = new AdminAdapter(users);
                            binding.usersRecycler.setAdapter(adminAdapter);
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
        binding.errorMessage.setText(String.format("%s", "No Users Found"));
    }


}