package com.example.facilityapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.facilityapp.databinding.ItemContainerHistoryBinding;
import com.example.facilityapp.listeners.ReportListener;

import com.example.facilityapp.models.History;


import java.util.List;

public class ReportHistoryAdapter extends RecyclerView.Adapter<ReportHistoryAdapter.UserViewHolder>{


    private final List<History> reportHistory;
    private final ReportListener reportListener;



    public ReportHistoryAdapter(List<History> reportHistory, ReportListener reportListener) {
        this.reportHistory= reportHistory;
        this.reportListener = reportListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerHistoryBinding itemContainerHistoryBinding = ItemContainerHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerHistoryBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(reportHistory.get(position));
    }

    @Override
    public int getItemCount() {
        return reportHistory.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        ItemContainerHistoryBinding binding;

        UserViewHolder(ItemContainerHistoryBinding itemContainerHistoryBinding){
            super(itemContainerHistoryBinding.getRoot());
            binding = itemContainerHistoryBinding;
        }

        void setUserData(History history){
            binding.textName.setText(history.name);
            binding.textDateTime.setText(history.dateTime);
            binding.imageProfile.setImageBitmap(getUserImage(history.image));
            binding.Reportid.setText(history.reportid);
            binding.UserId.setText(history.id);
            binding.status.setText(history.status);
            binding.getRoot().setOnClickListener(v -> reportListener.onClickedReport(history));
        }

    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

