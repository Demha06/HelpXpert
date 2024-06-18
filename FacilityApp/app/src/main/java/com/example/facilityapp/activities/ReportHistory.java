package com.example.facilityapp.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.facilityapp.R;
import com.example.facilityapp.adapters.ReportHistoryAdapter;
import com.example.facilityapp.adapters.UsersAdapter;
import com.example.facilityapp.databinding.ActivityReportHistoryBinding;
import com.example.facilityapp.databinding.ItemContainerHistoryBinding;
import com.example.facilityapp.listeners.ReportListener;
import com.example.facilityapp.models.Form;
import com.example.facilityapp.models.History;
import com.example.facilityapp.models.User;
import com.example.facilityapp.utilities.PreferenceManager;
import com.example.facilityapp.utilities.constants;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ReportHistory extends BaseActivity implements ReportListener {

    private ActivityReportHistoryBinding binding;
    private ItemContainerHistoryBinding bind;
    private PreferenceManager preferenceManager;
    private ListenerRegistration listenerRegistration;
    private Form form;
    private List<History> history;
    private ReportHistoryAdapter reportHistoryAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportHistoryBinding.inflate(getLayoutInflater());
        bind = ItemContainerHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        form = new Form();
        history = new ArrayList<>();
        reportHistoryAdapter = new ReportHistoryAdapter(history, this);

        loadReports();
        setListeners();
        loadUserDetails();
        getToken();



    }


    private void loadReports() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        listenerRegistration = database.collection(constants.KEY_COLLECTION_HISTORY)
                .orderBy(constants.KEY_TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    loading(false);
                    if (error != null) {
                        showErrorMessage();
                        return;
                    }

                    List<History> report = new ArrayList<>();
                    ReportHistoryAdapter reportHistoryAdapter = new ReportHistoryAdapter(report, this);

                    for (DocumentChange documentChange : value.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            History reportHistory = new History();
                            reportHistory.name = documentChange.getDocument().getString(constants.KEY_REPORTER);
                            reportHistory.dateTime = getReadableDateTime(documentChange.getDocument().getDate(constants.KEY_TIMESTAMP));
                            reportHistory.image = documentChange.getDocument().getString(constants.KEY_REPORTERIMAGE);
                            reportHistory.id = documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                            reportHistory.reportid = documentChange.getDocument().getString(constants.KEY_REPORTID);
                            reportHistory.status = documentChange.getDocument().getString(constants.KEY_STATUS);
                            report.add(reportHistory);
                        }
                        if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                            for (int i = 0; i < report.size(); i++) {
                                if (report.get(i).reportid.equals(documentChange.getDocument().getString(constants.KEY_REPORTID))) {
                                    report.get(i).status = documentChange.getDocument().getString(constants.KEY_STATUS);
                                    reportHistoryAdapter.notifyItemChanged(i);
                                    break;
                                }
                            }
                        }
                        if (documentChange.getType() == DocumentChange.Type.REMOVED) {
                            for (int i = 0; i < report.size(); i++) {
                                if (report.get(i).reportid.equals(documentChange.getDocument().getString(constants.KEY_REPORTID))) {
                                    report.remove(i);
                                    reportHistoryAdapter.notifyItemRemoved(i);
                                    break;
                                }
                            }
                        }
                    }

                    if (report.size() > 0) {
                        binding.historyRecycler.setAdapter(reportHistoryAdapter);
                        binding.historyRecycler.setVisibility(View.VISIBLE);
                    } else {
                        showErrorMessage();
                    }
                });
    }



    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
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

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMM dd, yyy - hh:mm a", Locale.getDefault()).format(date);
    }

    public void setListeners(){
        binding.ImageSettings.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        });
        binding.ChatImage.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),ConversationsActivity.class));
            overridePendingTransition(R.anim.right, R.anim.left);

        });
        binding.reload.setOnClickListener(v -> loadReports());
    }

    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.left, R.anim.right);
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(constants.KEY_COLLECTION_FACILITY).document(
                        preferenceManager.getString(constants.KEY_USER_ID)
                );
        documentReference.update(constants.KEY_FCM_TOKEN, token)

                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                        "Unable to update Token", Toast.LENGTH_SHORT).show());
    }

    public void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.ImageProfile.setImageBitmap(bitmap);
    }

    @Override
    public void onClickedReport(History history) {
        preferenceManager.putString(constants.REPO_ID, history.reportid);
       // form.reportId = preferenceManager.getString(constants.REPO_ID);

        preferenceManager.putString(constants.USE_ID, history.id);
        form.reportId = preferenceManager.getString(constants.USE_ID);

        Intent intent = new Intent(getApplicationContext(), ReportInformationActivity.class);
        startActivity(intent);

        Toast.makeText(this, form.reportId, Toast.LENGTH_SHORT).show();
    }

}


