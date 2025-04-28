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

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<Profile> profileData;
    private OnProfileClickListener listener;

    public ProfileAdapter(List<Profile> profileData, OnProfileClickListener listener) {
        this.profileData = profileData;
        this.listener = listener;
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

        String base64Image = profile.getBase64Image();
        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.profileImageView.setImageBitmap(decodedByte);
        }
        Button btnFriendRequest = holder.itemView.findViewById(R.id.btnFriendRequest);
        if (btnFriendRequest != null) {
            btnFriendRequest.setVisibility(View.GONE);
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

        public ProfileViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            quoteTextView = itemView.findViewById(R.id.quoteTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
        }
    }
}
