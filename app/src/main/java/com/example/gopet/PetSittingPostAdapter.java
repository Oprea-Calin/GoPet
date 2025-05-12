package com.example.gopet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PetSittingPostAdapter extends RecyclerView.Adapter<PetSittingPostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(PetSittingPost post);
    }

    private List<PetSittingPost> postList;
    private Context context;
    private OnPostClickListener listener;

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
        holder.textViewLocation.setText(post.location);
        holder.textViewNotes.setText(post.notes);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPostClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewLocation, textViewNotes;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewNotes = itemView.findViewById(R.id.textViewNotes);
        }
    }
}
