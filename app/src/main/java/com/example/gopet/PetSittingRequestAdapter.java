// PetSittingRequestAdapter.java
package com.example.gopet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PetSittingRequestAdapter extends RecyclerView.Adapter<PetSittingRequestAdapter.RequestViewHolder> {

    public interface OnAcceptClickListener {
        void onAcceptClicked(PetSittingRequest request);
    }

    private List<PetSittingRequest> requestList;
    private OnAcceptClickListener listener;

    public PetSittingRequestAdapter(List<PetSittingRequest> requestList, OnAcceptClickListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet_sitting_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        PetSittingRequest request = requestList.get(position);
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(request.userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        holder.textUserName.setText("User name: " + username);
                    } else {
                        holder.textUserName.setText("User name:");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.textUserName.setText("User name:");
                });
        holder.btnAccept.setOnClickListener(v -> listener.onAcceptClicked(request));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView textUserName;
        Button btnAccept;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.textUserName);
            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }
}