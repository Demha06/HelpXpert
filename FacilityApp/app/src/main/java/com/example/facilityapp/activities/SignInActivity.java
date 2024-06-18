package com.example.facilityapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.facilityapp.databinding.ActivitySignInBinding;
import com.example.facilityapp.utilities.PreferenceManager;
import com.example.facilityapp.utilities.constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(getApplicationContext());

        if (preferenceManager.getBoolean(constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), ReportHistory.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
    }

    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails() == true)
                signIn();
        });
        binding.textForgotPassword.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class)));

    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(constants.KEY_COLLECTION_FACILITY)
                .whereEqualTo(constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null &&
                    task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(constants. KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(constants. KEY_NAME, documentSnapshot.getString(constants.KEY_NAME));
                        preferenceManager.putString(constants. KEY_IMAGE, documentSnapshot.getString(constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), ReportHistory.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent. FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else {
                        loading(false);
                        showToast("Credentials are not correct");
                    }
                });

    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().isEmpty()){
            showToast("Enter an Email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid Email");
            return false;
        }else if(binding.inputPassword.getText().toString().isEmpty()){
            showToast("Enter a Password");
            return false;
        }else
            return true;
    }

    private void loading(boolean isLoading){
        if (isLoading == true){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }
}