package com.example.gopet;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private final List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment c = comments.get(position);
        holder.username.setText(c.authorUsername);
        holder.timestamp.setText(c.timestamp);
        holder.content.setText(c.comment);
        if (c.authorImageBase64 != null && !c.authorImageBase64.isEmpty()) {
            byte[] decoded = Base64.decode(c.authorImageBase64, Base64.DEFAULT);
            holder.image.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView username, timestamp, content;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageProfile);
            username = itemView.findViewById(R.id.textUsername);
            timestamp = itemView.findViewById(R.id.textTimestamp);
            content = itemView.findViewById(R.id.textComment);
        }
    }
}

