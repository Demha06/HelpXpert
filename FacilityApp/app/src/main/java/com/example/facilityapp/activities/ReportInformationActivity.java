package com.example.facilityapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.facilityapp.R;
import com.example.facilityapp.databinding.ActivityReportInformationBinding;
import com.example.facilityapp.databinding.ItemContainerHistoryBinding;
import com.example.facilityapp.models.Form;
import com.example.facilityapp.models.History;
import com.example.facilityapp.utilities.PreferenceManager;
import com.example.facilityapp.utilities.constants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ReportInformationActivity extends AppCompatActivity {

    private ActivityReportInformationBinding binding;
    private ItemContainerHistoryBinding bind;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private History reportHistory;
    private Form form;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportInformationBinding.inflate(getLayoutInflater());
        bind = ItemContainerHistoryBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        reportHistory = new History();
        form = new Form();

        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());



        displayReportInfo();
        setListeners();
    }

    public void setListeners(){

        String status = preferenceManager.getString(constants.KEY_STATUS);

        if (status != null && status.equals("Resolved")){
            ColorStateList resolvedTint = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error));
            binding.Resolved.setBackgroundTintList(resolvedTint);

            ColorStateList unresolvedTint = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.secondary_text));
            binding.Unresolved.setBackgroundTintList(unresolvedTint);

        }else if (status != null && status.equals("Unresolved")){
            ColorStateList resolvedTint = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.secondary_text));
            binding.Resolved.setBackgroundTintList(resolvedTint);

            ColorStateList unresolvedTint = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error));
            binding.Unresolved.setBackgroundTintList(unresolvedTint);
        }


        binding.back.setOnClickListener(v -> onBackPressed());

        binding.Resolved.setOnClickListener(v -> {
            reportStatusSolved();
            ColorStateList resolvedTint = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error));
            binding.Resolved.setBackgroundTintList(resolvedTint);

            ColorStateList unresolvedTint = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.secondary_text));
            binding.Unresolved.setBackgroundTintList(unresolvedTint);

            preferenceManager.putBoolean("isResolved", true);

        });

        binding.Unresolved.setOnClickListener(v ->{
            reportStatusUnsolved();
            ColorStateList resolvedTint = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.secondary_text));
            binding.Resolved.setBackgroundTintList(resolvedTint);

            ColorStateList unresolvedTint = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.error));
            binding.Unresolved.setBackgroundTintList(unresolvedTint);

            preferenceManager.putBoolean("isResolved", false);

        });

    }


    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMM dd, yyy - hh:mm a", Locale.getDefault()).format(date);
    }

    public void displayReportInfo(){
        loading(true);
        database.collection(constants.KEY_COLLECTION_HISTORY)
                .whereEqualTo(constants.KEY_REPORTID, preferenceManager.getString(constants.REPO_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null &&
                            task.getResult().getDocuments().size() > 0) {

                        DocumentSnapshot document = task.getResult().getDocuments().get(0);

                        preferenceManager.putString(constants.KEY_STATUS, document.getString(constants.KEY_STATUS));

                        binding.reportId.setText(document.getString(constants.KEY_REPORTID));
                        binding.description.setText(document.getString(constants.KEY_DESCRIPTION));
                        binding.handlerName.setText(document.getString(constants.KEY_HANDLERNAME));
                        binding.reporterName.setText(document.getString(constants.KEY_REPORTER));
                        binding.timeStamp.setText(getReadableDateTime(document.getDate(constants.KEY_TIMESTAMP)));
                        loading(false);




                    } else {
                        // Handle the error
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

    private void reportStatusSolved(){
        database.collection(constants.KEY_COLLECTION_HISTORY)
                .whereEqualTo(constants.KEY_REPORTID, preferenceManager.getString(constants.REPO_ID))
                .whereEqualTo(constants.KEY_RECEIVER_ID, preferenceManager.getString(constants.USE_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String documentId = document.getId();
                        DocumentReference documentReference = database.collection(constants.KEY_COLLECTION_HISTORY).document(documentId);
                        documentReference.update(constants.KEY_STATUS, "Resolved")
                                .addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(),
                                        "Report Status updated to 'Resolved'", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                                        "Unable to update report status", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                        "Unable to retrieve report", Toast.LENGTH_SHORT).show());
    }



    private void reportStatusUnsolved(){
        database.collection(constants.KEY_COLLECTION_HISTORY)
                .whereEqualTo(constants.KEY_REPORTID, preferenceManager.getString(constants.REPO_ID))
                .whereEqualTo(constants.KEY_RECEIVER_ID, preferenceManager.getString(constants.USE_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String documentId = document.getId();
                        DocumentReference documentReference = database.collection(constants.KEY_COLLECTION_HISTORY).document(documentId);
                        documentReference.update(constants.KEY_STATUS, "Unresolved")
                                .addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(),
                                        "Report Status updated to 'Unresolved'", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                                        "Unable to update report status", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                        "Unable to retrieve report", Toast.LENGTH_SHORT).show());
    }


}