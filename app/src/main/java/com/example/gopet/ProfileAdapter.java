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

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<Profile> profileData;
    private OnProfileClickListener listener;
    private boolean showActions;

    public ProfileAdapter(List<Profile> profileData, OnProfileClickListener listener, boolean showActions) {
        this.profileData = profileData;
        this.listener = listener;
        this.showActions = showActions;
    }

    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProfileViewHolder holder, int position) {
        Profile profile = profileData.get(position);
        holder.usernameTextView.setText(profile.getUsername());
        holder.quoteTextView.setText(profile.getQuote());

        if (profile.getQuote() != null && !profile.getQuote().isEmpty()) {
            holder.quoteTextView.setVisibility(View.VISIBLE);
        } else {
            holder.quoteTextView.setVisibility(View.GONE);
        }

        String base64Image = profile.getBase64Image();
        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Glide.with(holder.itemView)
                    .asBitmap()
                    .load(decodedByte)
                    .circleCrop()
                    .into(holder.profileImageView);

        }

        if (showActions) {
            holder.btnFriendRequest.setVisibility(View.VISIBLE);
            holder.btnShareAnimals.setVisibility(View.VISIBLE);
            holder.btnAcceptShare.setVisibility(View.VISIBLE);
            holder.btnCancelShare.setVisibility(View.VISIBLE);
            holder.btnRemoveFriend.setVisibility(View.VISIBLE);
        } else {
            holder.btnFriendRequest.setVisibility(View.GONE);
            holder.btnShareAnimals.setVisibility(View.GONE);
            holder.btnAcceptShare.setVisibility(View.GONE);
            holder.btnCancelShare.setVisibility(View.GONE);
            holder.btnRemoveFriend.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProfileClick(profile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return profileData.size();
    }

    public interface OnProfileClickListener {
        void onProfileClick(Profile profile);
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView quoteTextView;
        ImageView profileImageView;
        Button btnFriendRequest, btnShareAnimals, btnAcceptShare, btnCancelShare, btnRemoveFriend;

        public ProfileViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            quoteTextView = itemView.findViewById(R.id.quoteTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);

            btnFriendRequest = itemView.findViewById(R.id.btnFriendRequest);
            btnShareAnimals = itemView.findViewById(R.id.btnShareAnimals);
            btnAcceptShare = itemView.findViewById(R.id.btnAcceptShare);
            btnCancelShare = itemView.findViewById(R.id.btnCancelShare);
            btnRemoveFriend = itemView.findViewById(R.id.btnRemoveFriend);
        }
    }
}
