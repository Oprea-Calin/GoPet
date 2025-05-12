package com.example.gopet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class PetSittingPostAdapter extends RecyclerView.Adapter<PetSittingPostAdapter.PostViewHolder> {

    private final List<PetSittingPost> postList;
    private final Context context;
    private final OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(PetSittingPost post);
    }

    public PetSittingPostAdapter(Context context, List<PetSittingPost> postList, OnPostClickListener listener) {
        this.context = context;
        this.postList = postList;
        this.listener = listener;
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

        holder.textViewDate.setText(post.startDate + " - " + post.endDate);
        holder.textViewLocation.setText("Locație: " + post.location);
        holder.textViewNotes.setText("Detalii: " + post.notes);

        if (post.ownerUsername != null) {
            holder.textViewOwner.setText("Postat de: " + post.ownerUsername);
        } else {
            FirebaseFirestore.getInstance().collection("users")
                    .document(post.ownerId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String username = doc.getString("username");
                        post.ownerUsername = username;
                        holder.textViewOwner.setText("Postat de: " + username);
                    });
        }
        if (!post.isActive && post.acceptedUserId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(post.acceptedUserId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String name = doc.getString("username");
                        holder.textViewAccepted.setText("Acceptat de: " + name);
                        holder.textViewAccepted.setVisibility(View.VISIBLE);
                    });
        } else {
            holder.textViewAccepted.setVisibility(View.GONE);
        }


        StringBuilder animalDetails = new StringBuilder();
        holder.textViewAnimalDetails.setText("Încărcare animale...");
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

        holder.itemView.setOnClickListener(v -> listener.onPostClick(post));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewLocation, textViewNotes, textViewOwner, textViewAnimalDetails, textViewAccepted;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewNotes = itemView.findViewById(R.id.textViewNotes);
            textViewOwner = itemView.findViewById(R.id.textViewOwner);
            textViewAnimalDetails = itemView.findViewById(R.id.textViewAnimalDetails);
            textViewAccepted = itemView.findViewById(R.id.textViewAccepted);
        }
    }
}
