package com.example.facilityapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.facilityapp.databinding.ActivityFormBinding;
import com.example.facilityapp.models.Form;
import com.example.facilityapp.models.User;
import com.example.facilityapp.utilities.PreferenceManager;
import com.example.facilityapp.utilities.constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;

public class FormActivity extends AppCompatActivity {

    private ActivityFormBinding binding;
    private FirebaseFirestore database;
    private User receiverUser = new User();
    private Form form = new Form();
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();

    }

    public void setListeners(){
      binding.submitButton.setOnClickListener(v -> {
          sendFormInfo();
          binding.description.setText("");
          binding.reportId.setText("");
          binding.handlerName.setText("");
      });
      binding.back.setOnClickListener(v -> onBackPressed());
    }

    private void sendFormInfo(){
        if (!TextUtils.isEmpty(binding.handlerName.getText().toString()) &&
                !TextUtils.isEmpty(binding.reportId.getText().toString()) &&
                !TextUtils.isEmpty(binding.description.getText().toString())) {

            preferenceManager.putString(constants.KEY_HANDLERNAME, binding.handlerName.getText().toString());
            preferenceManager.putString(constants.KEY_REPORTID, binding.reportId.getText().toString());
            preferenceManager.putString(constants.KEY_DESCRIPTION, binding.description.getText().toString());

            HashMap<String, Object> report = new HashMap<>();
            report.put(constants.KEY_REPORTID, preferenceManager.getString(constants.KEY_REPORTID));
            report.put(constants.KEY_HANDLERNAME, preferenceManager.getString(constants.KEY_HANDLERNAME));
            report.put(constants.KEY_DESCRIPTION, preferenceManager.getString(constants.KEY_DESCRIPTION));
            report.put(constants.KEY_REPORTER, preferenceManager.getString(constants.KEY_REPORTER));
            report.put(constants.KEY_REPORTERIMAGE, preferenceManager.getString(constants.KEY_REPORTERIMAGE));
            report.put(constants.KEY_RECEIVER_ID, preferenceManager.getString(constants.KEY_RECEIVER_ID));
            report.put(constants.KEY_TIMESTAMP, new Date());
            report.put(constants.KEY_STATUS, "");

            database.collection(constants.KEY_COLLECTION_HISTORY)
                    .add(report)
                    .addOnSuccessListener(message -> {
                        Toast.makeText(this, "Report is successful", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(error -> {
                        Toast.makeText(this, "Report is unsuccessful", Toast.LENGTH_SHORT).show();
                    });
        }else
            Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
    }



}