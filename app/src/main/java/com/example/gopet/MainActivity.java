package com.example.gopet;

import android.content.Intent;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
import android.os.Bundle;
=======
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
=======
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
>>>>>>> Stashed changes
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
>>>>>>> Stashed changes
import android.view.View;
import android.widget.EditText;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
<<<<<<< Updated upstream
=======
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
>>>>>>> Stashed changes

public class MainActivity extends AppCompatActivity {

<<<<<<< Updated upstream
    EditText editUsernameLog , editPasswordLog;
    Button btnLogin, btnRegister;
=======
    private static final int PICK_IMAGE_REQUEST = 1;  // Codul pentru selecția imaginii
    private Uri imageUri;  // Variabila care va conține locația imaginii
>>>>>>> Stashed changes

<<<<<<< Updated upstream
=======
    FirebaseAuth auth;
    ImageView logoutImage, settingsImage, selectedImageView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    RecyclerView animalsView;
    LinearLayout addAnimalFormLayout;
    FirebaseFirestore database;
    animalsListAdapter animals_listAdapter;
    ArrayList<animal> animals;
    EditText animalNameEdit, animalAgeEdit, animalBreedEdit;
<<<<<<< Updated upstream
    Button submitAnimalFormButton, addAnimal;
    static final int PICK_IMAGE_REQUEST = 1;
    Uri imageUri;
    ImageView selectedImageView;
>>>>>>> Stashed changes

=======
    Button submitAnimalFormButton, addAnimal, selectImageButton;
>>>>>>> Stashed changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editUsernameLog = findViewById(R.id.inUsername);
        editPasswordLog = findViewById(R.id.inPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
<<<<<<< Updated upstream

=======
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
        selectImageButton = findViewById(R.id.selectImageButton);
        selectedImageView = findViewById(R.id.selectedImageView);  // ImageView pentru a afișa imaginea selectată

        addAnimal.setOnClickListener(v -> addAnimalFormLayout.setVisibility(View.VISIBLE));

<<<<<<< Updated upstream
        Button selectImageButton = findViewById(R.id.selectImageButton);
        selectedImageView = findViewById(R.id.selectedImageView);

        selectImageButton.setOnClickListener(v -> openGallery());
=======
        selectImageButton.setOnClickListener(v -> openGallery());  // Deschide galeria pentru selecția imaginii
>>>>>>> Stashed changes

        submitAnimalFormButton.setOnClickListener(v -> {
            String name = animalNameEdit.getText().toString();
            String age = animalAgeEdit.getText().toString();
            String breed = animalBreedEdit.getText().toString();

<<<<<<< Updated upstream
            if (name.isEmpty() || age.isEmpty() || breed.isEmpty()) {
                Toast.makeText(this, "Completeaza toate detaliile!", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    // Înlocuirea imageUri cu Base64
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    String base64Image = convertImageToBase64(bitmap);
                    saveAnimalToDatabase(name, age, breed, base64Image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
>>>>>>> Stashed changes
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view)
            {
                Intent i = new Intent(MainActivity.this, Register.class);
                startActivity(i);

            }
        });


<<<<<<< Updated upstream

=======
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                selectedImageView.setImageBitmap(bitmap);

                String base64Image = convertImageToBase64(bitmap);

                String name = animalNameEdit.getText().toString();
                String age = animalAgeEdit.getText().toString();
                String breed = animalBreedEdit.getText().toString();

                if(name.isEmpty() || age.isEmpty() || breed.isEmpty()) {
                    Toast.makeText(this, "Completeaza toate detaliile!", Toast.LENGTH_SHORT).show();
                } else {
                    saveAnimalToDatabase(name, age, breed, base64Image);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void saveAnimalToDatabase(String name, String age, String breed, String base64Image) {
=======
            if(name.isEmpty() || age.isEmpty() || breed.isEmpty()) {
                Toast.makeText(this, "Completeaza toate detaliile!", Toast.LENGTH_SHORT).show();
            } else {
                // Convertește imaginea în Base64 dacă există
                String base64Image = "";
                if (imageUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        base64Image = convertImageToBase64(bitmap);  // convertește imaginea în Base64
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Salvează animalul în Firestore cu imaginea în Base64
                saveAnimaltoDatabase(name, age, breed, base64Image);
            }
        });

        loadAnimals();

        settingsImage.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            finish();
        });

        logoutImage.setOnClickListener(view -> logout());
    }

    private void saveAnimaltoDatabase(String name, String age, String breed, String base64Image) {
>>>>>>> Stashed changes
        String animalID = database.collection("Animals").document().getId();
        animal newAnimal = new animal(name, breed, age, base64Image);

        database.collection("Animals").document(animalID)
                .set(newAnimal)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Animal adaugat!", Toast.LENGTH_SHORT).show();
                    addAnimalFormLayout.setVisibility(View.GONE);
<<<<<<< Updated upstream

                    animals.add(newAnimal);
                    animals_listAdapter.notifyItemInserted(animals.size() - 1);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "error loading animals", e);
                    Toast.makeText(MainActivity.this, "Eroare la adaugarea animalului", Toast.LENGTH_SHORT).show();
                });
    }

=======

                    animals.add(newAnimal);
                    animals_listAdapter.notifyItemInserted(animals.size() - 1);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "error loading animals", e);
                    Toast.makeText(MainActivity.this, "Eroare la adaugarea animalului", Toast.LENGTH_SHORT).show();
                });
    }
>>>>>>> Stashed changes

    private String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void loadAnimals() {
        database.collection("Animals")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        animals.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            animal animal = document.toObject(animal.class);
                            if (animal != null) {
                                animals.add(animal);
                            }
                        }
                        animals_listAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "error loading animals", task.getException());
                        Toast.makeText(MainActivity.this, "Eroare la incarcarea animalelor!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void logout() {
        FirebaseAuth.getInstance().signOut();

        editor.clear();
        editor.apply();

        Intent i = new Intent(this, Login.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
>>>>>>> Stashed changes
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);  // Deschide galeria pentru selectare imagine
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();  // Salvează locația imaginii selectate

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                selectedImageView.setImageBitmap(bitmap);  // Afișează imaginea selectată
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
