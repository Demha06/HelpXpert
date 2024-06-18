package com.example.facilityapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.facilityapp.databinding.ActivityForgotPasswordBinding;
import com.example.facilityapp.utilities.PreferenceManager;
import com.example.facilityapp.utilities.constants;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    public void setListeners(){
        binding.buttonResetPassword.setOnClickListener(v -> updatePassword());
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean checkDetails() {
        if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter new password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password does not match");
            return false;
        }
        return true;
    }

    private void updatePassword(){
        FirebaseFirestore database  = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = database.collection(constants.KEY_COLLECTION_FACILITY);
        collectionReference
                .whereEqualTo(constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(constants.KEY_SECRETPHRASE, binding.textSecretPhrase.getText().toString())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        showToast("Invalid email or secret phrase");
                    } else {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        preferenceManager.putString(constants. KEY_USER_ID, documentSnapshot.getId());
                        DocumentReference documentReference = collectionReference.document(preferenceManager.getString(constants.KEY_USER_ID));
                        if (checkDetails()){
                            documentReference.update(constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                                    .addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(),
                                            "Password has been updated", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                                            "Unable to update Password", Toast.LENGTH_SHORT).show());
                        }
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to update password"));
    }

}
