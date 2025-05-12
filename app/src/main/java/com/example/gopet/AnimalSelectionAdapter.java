// AnimalSelectionAdapter.java
package com.example.gopet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AnimalSelectionAdapter extends RecyclerView.Adapter<AnimalSelectionAdapter.AnimalViewHolder> {

    private List<animal> animals;
    private List<animal> selected;

    public AnimalSelectionAdapter(List<animal> animals) {
        this.animals = animals;
        this.selected = new ArrayList<>();
    }

    public void updateData(List<animal> newAnimals) {
        animals.clear();
        animals.addAll(newAnimals);
        selected.clear();
        notifyDataSetChanged();
    }

    public List<animal> getSelectedAnimals() {
        return selected;
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_animal_selectable, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        animal a = animals.get(position);
        holder.name.setText(a.getName());
        holder.checkBox.setChecked(selected.contains(a));

        View.OnClickListener toggleSelection = v -> {
            if (selected.contains(a)) {
                selected.remove(a);
                holder.checkBox.setChecked(false);
            } else {
                selected.add(a);
                holder.checkBox.setChecked(true);
            }
        };

        holder.itemView.setOnClickListener(toggleSelection);
        holder.checkBox.setOnClickListener(toggleSelection);
    }


    @Override
    public int getItemCount() {
        return animals.size();
    }

    static class AnimalViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CheckBox checkBox;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textAnimalName);
            checkBox = itemView.findViewById(R.id.checkboxAnimal);
        }
    }
}