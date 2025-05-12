// CreatePetSittingPostActivity.java
package com.example.gopet;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class CreatePetSittingPostActivity extends AppCompatActivity {

    private EditText locationInput, notesInput, startDateInput, endDateInput;
    private Button postButton;
    private RecyclerView animalRecyclerView;
    private AnimalSelectionAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pet_sitting_post);

        locationInput = findViewById(R.id.input_location);
        notesInput = findViewById(R.id.input_notes);
        startDateInput = findViewById(R.id.input_start_date);
        endDateInput = findViewById(R.id.input_end_date);
        postButton = findViewById(R.id.btn_post);
        animalRecyclerView = findViewById(R.id.recycler_animals);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        animalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnimalSelectionAdapter(new ArrayList<>());
        animalRecyclerView.setAdapter(adapter);

        loadUserAnimals();
        startDateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String dateStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                startDateInput.setText(dateStr);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        endDateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String dateStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                endDateInput.setText(dateStr);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        postButton.setOnClickListener(v -> {
            List<animal> selected = adapter.getSelectedAnimals();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Selectează cel puțin un animal", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> selectedIds = new ArrayList<>();
            for (animal a : selected) {
                selectedIds.add(a.getId());
            }

            String postId = UUID.randomUUID().toString();
            String ownerId = auth.getCurrentUser().getUid();
            String start = startDateInput.getText().toString();
            String end = endDateInput.getText().toString();
            String loc = locationInput.getText().toString();
            String notes = notesInput.getText().toString();

            PetSittingPost post = new PetSittingPost(postId, ownerId, selectedIds, start, end, loc, notes);
            db.collection("petSittingPosts").document(postId).set(post)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Anunț publicat!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Eroare la publicare", Toast.LENGTH_SHORT).show());
        });

        startDateInput.setOnClickListener(v -> showDatePickerDialog(startDateInput));
        endDateInput.setOnClickListener(v -> showDatePickerDialog(endDateInput));
    }

    private void loadUserAnimals() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).collection("Animals")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<animal> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        animal a = doc.toObject(animal.class);
                        a.setId(doc.getId());
                        list.add(a);
                    }
                    adapter.updateData(list);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Eroare la încărcare animale", Toast.LENGTH_SHORT).show());
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    editText.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }
}