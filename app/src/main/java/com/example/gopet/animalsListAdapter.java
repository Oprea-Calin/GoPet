package com.example.gopet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class animalsListAdapter extends RecyclerView.Adapter<animalsListAdapter.MyViewHolder> {

    Context context;
    ArrayList<animal> list;
    OnItemClickListener listener;
    OnItemClickListener deleteListener;

    public animalsListAdapter(ArrayList<animal> list, Context context, OnItemClickListener listener, OnItemClickListener deleteListener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_animal , parent, false);
        return new MyViewHolder(v);
    }
    public interface OnItemClickListener {
        void onItemClick(animal animal);
    }
    public interface OnItemClickDeleteListener{
        void onItemClick(animal animal);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        animal canimal = list.get(position);
        holder.age.setText(canimal.getCalculatedAge());
        holder.breed.setText(canimal.getBreed());
        holder.name.setText(canimal.getName());
        if (canimal.getBase64Image() != null) {
            byte[] decodedString = Base64.decode(canimal.getBase64Image(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.base64Image.setImageBitmap(decodedByte);
        } else {
            holder.base64Image.setImageResource(R.drawable.cat);
        }
        holder.itemView.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Alege acțiunea pentru " + canimal.getName())
                    .setItems(new CharSequence[]{"Editează", "Șterge"}, (dialog, which) -> {
                        if (which == 0) {
                            if (deleteListener != null) {
                                deleteListener.onItemClick(canimal);
                            }
                        } else if (which == 1) {
                            if (listener != null) {
                                listener.onItemClick(canimal);
                            }
                        }
                    })
                    .show();
        });



    }
    private String calculeazaVarstaDinData(String dataNasteriiStr) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        try {
            java.util.Date dataNasterii = sdf.parse(dataNasteriiStr);
            Calendar birth = Calendar.getInstance();
            birth.setTime(dataNasterii);

            Calendar today = Calendar.getInstance();

            int ani = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            int luni = today.get(Calendar.MONTH) - birth.get(Calendar.MONTH);

            if (today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH)) {
                luni--;
            }

            if (luni < 0) {
                ani--;
                luni += 12;
            }

            String aniText = ani + " " + (ani == 1 ? "an" : "ani");
            String luniText = luni + " " + (luni == 1 ? "lună" : "luni");

            if (ani == 0) return luniText;
            if (luni == 0) return aniText;
            return aniText + " și " + luniText;
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView name, breed, age;
        ImageView base64Image;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            breed = itemView.findViewById(R.id.itemBreed);
            age = itemView.findViewById(R.id.itemAge);
            base64Image = itemView.findViewById(R.id.animalImageView);
        }
    }
}
