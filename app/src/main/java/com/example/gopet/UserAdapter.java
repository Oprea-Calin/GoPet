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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<DocumentSnapshot> users;
    private List<DocumentSnapshot> friends;
    private OnAddFriendClickListener listener;

    public UserAdapter(List<DocumentSnapshot> users,List<DocumentSnapshot> friends, OnAddFriendClickListener listener) {
        this.users = users;
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        DocumentSnapshot userDocument = users.get(position);
        String username = userDocument.getString("username");
        String base64Image = userDocument.getString("base64Image");
        String userId = userDocument.getId();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        holder.usernameTextView.setText(username != null ? username : "Fără nume");

        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.profileImageView.setImageBitmap(decodedByte);
        }

        boolean isAlreadyFriend = false;
        String friendStatus = null;
        for (DocumentSnapshot friend : friends) {
            if (friend.getId().equals(userId)) {
                isAlreadyFriend = true;
                friendStatus = friend.getString("status");
                break;
            }
        }

        holder.btnFriendRequest.setVisibility(View.GONE);
        holder.btnShareAnimals.setVisibility(View.GONE);
        holder.btnAcceptShare.setVisibility(View.GONE);

        if (userId.equals(currentUserId)) {
            holder.btnFriendRequest.setVisibility(View.GONE);
            holder.btnShareAnimals.setVisibility(View.GONE);
        } else {
            if (isAlreadyFriend) {
                if ("pending".equals(friendStatus)) {
                    holder.btnFriendRequest.setVisibility(View.VISIBLE);
                    holder.btnFriendRequest.setText("Cerere trimisă");
                    holder.btnFriendRequest.setEnabled(false);
                } else if ("confirmed".equals(friendStatus)) {
                    holder.btnFriendRequest.setVisibility(View.VISIBLE);
                    holder.btnFriendRequest.setText("Prieteni");
                    holder.btnFriendRequest.setEnabled(false);

                    holder.btnShareAnimals.setVisibility(View.VISIBLE);
                    holder.btnShareAnimals.setEnabled(true);
                    holder.btnShareAnimals.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onShareAnimalsClicked(userDocument);
                        }
                    });
                }
            } else {
                holder.btnFriendRequest.setVisibility(View.VISIBLE);
                holder.btnFriendRequest.setEnabled(true);
                holder.btnFriendRequest.setText("Adaugă în lista de prieteni");
                holder.btnFriendRequest.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddFriendClicked(userDocument);
                    }
                });
            }
        }


        checkForShareRequest(userId, holder);
    }


    private void checkForShareRequest(String fromUserId, UserViewHolder holder) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("sharedAnimalsRequests")
                .document(fromUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        if ("pending".equals(status)) {
                            holder.btnAcceptShare.setVisibility(View.VISIBLE);
                            holder.btnAcceptShare.setEnabled(true);
                            holder.btnAcceptShare.setText("Acceptă partajare");

                            holder.btnAcceptShare.setOnClickListener(v -> {
                                acceptShareRequest(fromUserId, holder);
                            });
                        } else {
                            holder.btnAcceptShare.setVisibility(View.GONE);
                        }
                    } else {
                        holder.btnAcceptShare.setVisibility(View.GONE);
                    }
                });
    }

    private void acceptShareRequest(String fromUserId, UserViewHolder holder) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("sharedAnimalsRequests")
                .document(fromUserId)
                .update("status", "accepted")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(holder.itemView.getContext(), "Partajare acceptată!", Toast.LENGTH_SHORT).show();
                    holder.btnAcceptShare.setVisibility(View.GONE);
                    if (holder.itemView.getContext() instanceof MainActivity) {
                        ((MainActivity) holder.itemView.getContext()).loadAnimals();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Eroare la acceptare partajare!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        ImageView profileImageView;
        Button btnFriendRequest, btnShareAnimals, btnAcceptShare;

        public UserViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            btnFriendRequest = itemView.findViewById(R.id.btnFriendRequest);
            btnShareAnimals = itemView.findViewById(R.id.btnShareAnimals);
            btnAcceptShare = itemView.findViewById(R.id.btnAcceptShare);
        }
    }

    public interface OnAddFriendClickListener {
        void onAddFriendClicked(DocumentSnapshot user);
        void onShareAnimalsClicked(DocumentSnapshot user);
    }
}
