package com.example.gopet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        holder.category.setText("Type: " + canimal.getCategory());
        holder.gender.setText("Gender: " + canimal.getGender());
        holder.weight.setText("Weight: " + (canimal.getWeight() != null ? canimal.getWeight() + " kg" : "N/A"));
        holder.reproductiveStatus.setText("Reproductive status: " + canimal.getReproductiveStatus());
        holder.allergies.setText("Alergies: " + canimal.getAllergies());

        if (canimal.getBase64Image() != null) {
            byte[] decodedString = Base64.decode(canimal.getBase64Image(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.base64Image.setImageBitmap(decodedByte);
        } else {
            holder.base64Image.setImageResource(R.drawable.cat);
        }
        if (canimal.isShared()) {
            holder.sharedBy.setVisibility(View.VISIBLE);
            holder.sharedBy.setText("Shared by: " + canimal.getSharedFromUsername());
        } else {
            holder.sharedBy.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Select action " + canimal.getName())
                    .setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
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
        holder.itemView.setOnLongClickListener(v -> {
            String breed = canimal.getBreed();
            String category = canimal.getCategory().toLowerCase();

            final String type;
            if (category.contains("pisi") || category.contains("cat")) {
                type = "cat";
            } else if (category.contains("caine") || category.contains("dog")) {
                type = "dog";
            } else {
                android.os.Handler handler = new android.os.Handler(context.getMainLooper());
                handler.post(() -> android.widget.Toast.makeText(context, "No info available for   \"" + canimal.getName() + "\"", Toast.LENGTH_SHORT).show());
                return true;
            }

            if (type.equals("cat")) {
                CatHelperAPI.fetchCatInfo(breed, new CatHelperAPI.CatInfoCallback() {
                    @Override
                    public void onResult(String name, String description, String temperament, String origin, String imageUrl) {
                        Intent intent = new Intent(context, BreedInfoActivity.class);
                        intent.putExtra("breed", breed);
                        intent.putExtra("type", type);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onError(String error) {
                        android.os.Handler handler = new android.os.Handler(context.getMainLooper());
                        handler.post(() -> android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show());
                    }
                });
            } else if (type.equals("dog")) {
                Intent intent = new Intent(context, BreedInfoActivity.class);
                intent.putExtra("breed", breed);
                intent.putExtra("type", type);
                context.startActivity(intent);
            }

            return true;
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

            String aniText = ani + " " + (ani == 1 ? "years" : "years");
            String luniText = luni + " " + (luni == 1 ? "years" : "years");

            if (ani == 0) return luniText;
            if (luni == 0) return aniText;
            return aniText + " and " + luniText;
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

        TextView name, breed, age, category, gender, weight, reproductiveStatus, allergies, sharedBy;
        ImageView base64Image;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            breed = itemView.findViewById(R.id.itemBreed);
            age = itemView.findViewById(R.id.itemAge);
            category = itemView.findViewById(R.id.itemCategory);
            gender = itemView.findViewById(R.id.itemGender);
            weight = itemView.findViewById(R.id.itemWeight);
            reproductiveStatus = itemView.findViewById(R.id.itemReproductiveStatus);
            allergies = itemView.findViewById(R.id.itemAllergies);
            base64Image = itemView.findViewById(R.id.animalImageView);
            sharedBy = itemView.findViewById(R.id.itemSharedBy);
        }
    }
}
