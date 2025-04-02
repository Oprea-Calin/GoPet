package com.example.gopet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class animalsListAdapter extends RecyclerView.Adapter<animalsListAdapter.MyViewHolder> {

    Context context;
    ArrayList<animal> list;

    public animalsListAdapter(ArrayList<animal> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_animal , parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        animal canimal = list.get(position);
        holder.age.setText(canimal.getAge());
        holder.breed.setText(canimal.getBreed());
        holder.name.setText(canimal.getName());
        String base64Image = canimal.getImageBase64();
        if (base64Image != null) {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.animalImage.setImageBitmap(decodedBitmap);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, breed, age;
        ImageView animalImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            breed = itemView.findViewById(R.id.itemBreed);
            age = itemView.findViewById(R.id.itemAge);
            animalImage = itemView.findViewById(R.id.animalImageView);
        }
    }

}
