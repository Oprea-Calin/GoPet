package com.example.gopet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    FirebaseAuth auth;
    ImageView logoutImage, settingsImage;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    RecyclerView animalsView;
    LinearLayout addAnimalFormLayout;
    FirebaseFirestore database;
    animalsListAdapter animals_listAdapter;
    ArrayList<animal> animals;
    EditText animalNameEdit, animalAgeEdit, animalBreedEdit;
    Button submitAnimalFormButton, addAnimal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        animalsView = findViewById(R.id.animalsView);
        database = FirebaseFirestore.getInstance();
        animalsView.setHasFixedSize(true);
        animalsView.setLayoutManager(new LinearLayoutManager(this));

        animals = new ArrayList<>();
        animals_listAdapter = new animalsListAdapter(animals, this);
        animalsView.setAdapter(animals_listAdapter);

        addAnimalFormLayout = findViewById(R.id.addAnimalFormLayout);
        animalAgeEdit = findViewById(R.id.animalAge);
        animalBreedEdit = findViewById(R.id.animalBreed);
        animalNameEdit = findViewById(R.id.animalName);
        submitAnimalFormButton = findViewById(R.id.addAnimalFormButton);

        logoutImage = findViewById(R.id.logoutImage);
        settingsImage = findViewById(R.id.settingsIcon);
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        auth = FirebaseAuth.getInstance();
        boolean rememberMe = sharedPreferences.getBoolean("remember", false);

        addAnimal = findViewById(R.id.addAnimalButton);
        addAnimal.setOnClickListener(v -> addAnimalFormLayout.setVisibility(View.VISIBLE));

        submitAnimalFormButton.setOnClickListener(v -> {
            String name = animalNameEdit.getText().toString();
            String age = animalAgeEdit.getText().toString();
            String breed = animalBreedEdit.getText().toString();

            if(name.isEmpty() || age.isEmpty() || breed.isEmpty())
            {
                Toast.makeText(this, "Completeaza toate detaliile!", Toast.LENGTH_SHORT).show();
            }
            else{
                saveAnimaltoDatabase(name, age, breed);
            }

        });

        loadAnimals();


        settingsImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                finish();
            }
        });

        logoutImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                logout();
            }

        });
    }
    private void saveAnimaltoDatabase(String name, String age, String breed)
    {
        String animalID= database.collection("Animals").document().getId();
        animal newAnimal = new animal(name,breed, age);

        database.collection("Animals").document(animalID)
                .set(newAnimal)
                .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this,"Animal adaugat!", Toast.LENGTH_SHORT).show();
                        addAnimalFormLayout.setVisibility(View.GONE);

                        animals.add(newAnimal);
                        animals_listAdapter.notifyItemInserted(animals.size()-1);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore","error loading aniomals",e);
                        Toast.makeText(MainActivity.this, "Eroare la adaugarea animalului", Toast.LENGTH_SHORT).show();
                    });
        }

    private void loadAnimals(){

        database.collection("Animals")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        animals.clear();
                        for(DocumentSnapshot document : task.getResult()){
                            animal animal = document.toObject(animal.class);
                            if(animal != null)
                            {
                                animals.add(animal);
                            }
                        }
                        animals_listAdapter.notifyDataSetChanged();
                    }
                    else{
                        Log.e("Firestore","error loading aniomals",task.getException());
                        Toast.makeText(MainActivity.this, "Eroare la incarcarea animalelor!", Toast.LENGTH_SHORT).show();
                    }

                });

    }

    private void logout()
    {
        FirebaseAuth.getInstance().signOut();

        editor.clear();
        editor.apply();

        Intent i = new Intent(this, Login.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}