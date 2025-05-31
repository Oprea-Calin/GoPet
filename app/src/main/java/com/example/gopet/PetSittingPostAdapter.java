package com.example.gopet;

import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PetSittingPostAdapter extends RecyclerView.Adapter<PetSittingPostAdapter.PostViewHolder> {

    private final List<PetSittingPost> postList;
    private final Context context;
    private final OnPostClickListener listener;
    private final boolean isMyPosts;

    public interface OnPostClickListener {
        void onPostClick(PetSittingPost post);
        void onDeleteClick(PetSittingPost post);
        void onUsernameClick(PetSittingPost post);
    }

    public PetSittingPostAdapter(Context context, List<PetSittingPost> postList, OnPostClickListener listener, boolean isMyPosts) {
        this.context = context;
        this.postList = postList;
        this.listener = listener;
        this.isMyPosts=isMyPosts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pet_sitting_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PetSittingPost post = postList.get(position);

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);

        try {
            Date start = inputFormat.parse(post.startDate);
            Date end = inputFormat.parse(post.endDate);
            String formattedStart = outputFormat.format(start);
            String formattedEnd = outputFormat.format(end);
            holder.textViewDate.setText(
                    formattedStart.equals(formattedEnd)
                            ? "Date: " + formattedStart
                            : formattedStart + " - " + formattedEnd
            );
        } catch (Exception e) {
            holder.textViewDate.setText(post.startDate + " - " + post.endDate); // fallback
        }

        holder.textViewLocation.setText("Location: " + post.aproximative_location);
        holder.textViewNotes.setText("Notes: " + post.notes);

        if(post.price != null)
            holder.textViewPrice.setText("Pay: " + post.price);
        else holder.textViewPrice.setText("Pay: to be discussed.");

        FirebaseFirestore.getInstance().collection("users")
                .document(post.ownerId)
                .get()
                .addOnSuccessListener(doc -> {
                    String username = doc.getString("username");
                    String base64Image = doc.getString("base64Image");

                    holder.textViewOwner.setText("Posted by: " + username);

                    if (base64Image != null && !base64Image.isEmpty()) {
                        byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.imageProfile.setImageBitmap(decodedByte);
                    } else {
                    }
                });


        if (post.ownerUsername != null) {
            holder.textViewOwner.setText("Posted by: " + post.ownerUsername);
        } else {
            FirebaseFirestore.getInstance().collection("users")
                    .document(post.ownerId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String username = doc.getString("username");
                        post.ownerUsername = username;
                        holder.textViewOwner.setText("Posted by: " + username);
                    });
        }
        if (!post.isActive && post.acceptedUserId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(post.acceptedUserId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String name = doc.getString("username");
                        holder.textViewAccepted.setText("Accepted by: " + name);
                        holder.textViewAccepted.setVisibility(View.VISIBLE);
                    });
        } else {
            holder.textViewAccepted.setVisibility(View.GONE);
        }
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (isMyPosts && post.ownerId.equals(currentUserId)) {
            holder.buttonDeletePost.setVisibility(View.VISIBLE);
            holder.buttonDeletePost.setOnClickListener(v -> listener.onDeleteClick(post));
        } else {
            holder.buttonDeletePost.setVisibility(View.GONE);
        }
        if (post.isActive && post.ownerId.equals(currentUserId)) {
            FirebaseFirestore.getInstance()
                    .collection("petSittingRequests")
                    .whereEqualTo("postId", post.id)
                    .get()
                    .addOnSuccessListener(requests -> {
                        if (!requests.isEmpty()) {
                            holder.requestBadge.setVisibility(View.VISIBLE);
                        } else {
                            holder.requestBadge.setVisibility(View.GONE);
                        }
                    });
        } else {
            holder.requestBadge.setVisibility(View.GONE);
        }



        StringBuilder animalDetails = new StringBuilder();
        animalDetails.append("Included animals:"+'\n');
        holder.textViewAnimalDetails.setText("Pets loading");
        for (String animalId : post.animalIds) {
            FirebaseFirestore.getInstance()
                    .collection("users").document(post.ownerId)
                    .collection("Animals").document(animalId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        animal a = doc.toObject(animal.class);
                        if (a != null) {
                            animalDetails.append("• ")
                                    .append(a.getName())
                                    .append(" - ")
                                    .append(a.getBreed())
                                    .append("\n");
                            holder.textViewAnimalDetails.setText(animalDetails.toString());
                        }
                    });
        }

        holder.buttonDeletePost.setOnClickListener(v -> listener.onDeleteClick(post));
        holder.itemView.setOnClickListener(v -> listener.onPostClick(post));
        holder.textViewOwner.setOnClickListener(v -> listener.onUsernameClick(post));

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewLocation, textViewNotes, textViewOwner, textViewAnimalDetails, textViewAccepted, requestBadge, textViewPrice;

        ImageView imageProfile;
        Button buttonDeletePost;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewOwner = itemView.findViewById(R.id.textViewOwner);
            textViewAnimalDetails = itemView.findViewById(R.id.textViewAnimalDetails);
            textViewNotes = itemView.findViewById(R.id.textViewNotes);
            textViewAccepted = itemView.findViewById(R.id.textViewAccepted);
            requestBadge = itemView.findViewById(R.id.requestBadge);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            buttonDeletePost = itemView.findViewById(R.id.btnDeletePost);

        }
    }
}
