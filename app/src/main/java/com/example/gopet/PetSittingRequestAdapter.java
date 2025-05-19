package com.example.gopet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
                        String email = doc.getString("email");
                        String base64Image = doc.getString("base64Image");

                        if (base64Image != null && !base64Image.isEmpty()) {
                            byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            holder.imageProfile.setImageBitmap(bitmap);
                        }
                        holder.textUserName.setText(username != null ? username : "Unknown");
                        holder.textUserEmail.setText(email != null ? email : "No email");
                    } else {
                        holder.textUserName.setText("Unknown user");
                        holder.textUserEmail.setText("Unknown email");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.textUserName.setText("Error loading user");
                    holder.textUserEmail.setText("");
                });

        holder.btnAccept.setOnClickListener(v -> listener.onAcceptClicked(request));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView textUserName, textUserEmail;
        ImageView imageProfile;
        Button btnAccept;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.textUserName);
            textUserEmail = itemView.findViewById(R.id.textUserEmail);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            imageProfile = itemView.findViewById(R.id.imageProfile);
        }
    }
}
