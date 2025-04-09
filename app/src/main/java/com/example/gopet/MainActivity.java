package com.example.gopet;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

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
    EditText animalCategoryEdit, animalReproductiveStatusEdit, animalGenderEdit, animalWeightEdit, animalAllergiesEdit;
    Button submitAnimalFormButton;
    ImageView animalImageView, addAnimal;
    static final int PICK_IMAGE_REQUEST = 1;
    Uri imageUri;
    String selectedAnimalId = null;

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

        animalImageView = findViewById(R.id.animalImageView);
        Button selectImageButton = findViewById(R.id.addImageButton);
        selectImageButton.setOnClickListener(v -> openImageChooser());

        animalCategoryEdit = findViewById(R.id.animalCategory);
        animalReproductiveStatusEdit = findViewById(R.id.animalReproductiveStatus);
        animalGenderEdit = findViewById(R.id.animalGender);
        animalWeightEdit = findViewById(R.id.animalWeight);
        animalAllergiesEdit = findViewById(R.id.animalAllergies);


        animalsView = findViewById(R.id.animalsView);
        database = FirebaseFirestore.getInstance();
        animalsView.setHasFixedSize(true);
        animalsView.setLayoutManager(new LinearLayoutManager(this));

        animals = new ArrayList<>();
        animals_listAdapter = new animalsListAdapter(
                animals,
                this,
                animal -> { // deleteListener
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Ștergere animal")
                            .setMessage("Ești sigur că vrei să ștergi animalul " + animal.getName() + "?")
                            .setPositiveButton("Da", (dialog, which) -> deleteAnimal(animal))
                            .setNegativeButton("Nu", null)
                            .show();
                },
                animal -> { // editListener
                    populateFormWithAnimal(animal);
                }
        );
        animalsView.setAdapter(animals_listAdapter);

        animalAgeEdit = findViewById(R.id.animalBirthDate);
        addAnimalFormLayout = findViewById(R.id.addAnimalFormLayout);

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
        addAnimal.setOnClickListener(v -> {
            if (addAnimalFormLayout.getVisibility() == View.VISIBLE) {
                addAnimalFormLayout.setVisibility(View.GONE);
            } else {
                addAnimalFormLayout.setVisibility(View.VISIBLE);
            }
        });

        Calendar selectedDate = Calendar.getInstance();
        animalAgeEdit.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        selectedDate.set(year1, month1, dayOfMonth);
                        animalAgeEdit.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        submitAnimalFormButton.setOnClickListener(v -> {
            String name = animalNameEdit.getText().toString();
            String age = animalAgeEdit.getText().toString();
            String breed = animalBreedEdit.getText().toString();

            if(name.isEmpty() || age.isEmpty() || breed.isEmpty())
            {
                Toast.makeText(this, "Completeaza toate detaliile!", Toast.LENGTH_SHORT).show();
            }
            else{
                submitAnimalFormButton.setEnabled(false);
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
    private void deleteAnimal(animal animal) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        database.collection("users")
                .document(uid)
                .collection("Animals")
                .document(animal.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Animal șters", Toast.LENGTH_SHORT).show();
                    animals.remove(animal);
                    animals_listAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Eroare la ștergere", Toast.LENGTH_SHORT).show();
                });
    }


    private void populateFormWithAnimal(animal animal) {
        animalNameEdit.setText(animal.getName());
        animalBreedEdit.setText(animal.getBreed());
        animalAgeEdit.setText(animal.getAge());
        selectedAnimalId = animal.getId();

        if (animal.getBase64Image() != null && !animal.getBase64Image().isEmpty()) {
            byte[] decodedString = Base64.decode(animal.getBase64Image(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            animalImageView.setImageBitmap(decodedByte);
        } else {
            animalImageView.setImageDrawable(null);
        }

        addAnimalFormLayout.setVisibility(View.VISIBLE);
    }
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            animalImageView.setImageURI(imageUri);
        }
    }

    private String compressAndResizeImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            int maxWidth = 800;
            int maxHeight = 800;
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();

            float aspectRatio = (float) width / height;
            int newWidth = maxWidth;
            int newHeight = maxHeight;

            if (width > height) {
                newHeight = (int) (newWidth / aspectRatio);
            } else {
                newWidth = (int) (newHeight * aspectRatio);
            }

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);

            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String imageUriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

//    private void saveAnimaltoDatabase(String name, String age, String breed) {
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        String animalID = database.collection("users").document(uid).collection("Animals").document().getId();
//        animal newAnimal = new animal(name, breed, age);
//        newAnimal.setId(animalID);
//        String base64Image = "";
//        if (imageUri != null)
//        {
//            base64Image=compressAndResizeImage(imageUri);
//        }
//
//        if (base64Image != null) {
//            newAnimal.setBase64Image(base64Image);
//        }
//        newAnimal.setBase64Image(base64Image != null ? base64Image : "");
//
//        newAnimal.setBase64Image(base64Image);
//
//        database.collection("users")
//                .document(uid)
//                .collection("Animals")
//                .document(animalID)
//                .set(newAnimal)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(MainActivity.this, "Animal adaugat!", Toast.LENGTH_SHORT).show();
//                    addAnimalFormLayout.setVisibility(View.GONE);
//
//
////                    String varstaCalculata = calculeazaVarstaDinData(newAnimal.getAge());
////                    newAnimal.setAge(varstaCalculata);
////                    animals.add(newAnimal);
//
//                    addAnimalFormLayout.setVisibility(View.GONE);
//                    animalNameEdit.setText("");
//                    animalBreedEdit.setText("");
//                    animalAgeEdit.setText("");
//                    animalImageView.setImageDrawable(null);
//                    imageUri = null;
//
//                    loadAnimals();
//                    //animals_listAdapter.notifyItemInserted(animals.size() - 1);
//                    submitAnimalFormButton.setEnabled(true);
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("Firestore", "error loading animals", e);
//                    Toast.makeText(MainActivity.this, "Eroare la adaugarea animalului", Toast.LENGTH_SHORT).show();
//                    submitAnimalFormButton.setEnabled(true);
//                });
//    }
private void saveAnimaltoDatabase(String name, String age, String breed) {
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    String category = animalCategoryEdit.getText().toString();
    String reproductiveStatus = animalReproductiveStatusEdit.getText().toString();
    String gender = animalGenderEdit.getText().toString();
    String weightStr = animalWeightEdit.getText().toString();
    String allergies = animalAllergiesEdit.getText().toString();
    Float weight = weightStr.isEmpty() ? null : Float.parseFloat(weightStr);

    if (selectedAnimalId != null) {
        // UPDATE
        if (imageUri == null) {
            // nu s-a selectat imagine, o pastram pe cea din firebase
            database.collection("users")
                    .document(uid)
                    .collection("Animals")
                    .document(selectedAnimalId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String existingImage = documentSnapshot.getString("base64Image");

                        database.collection("users")
                                .document(uid)
                                .collection("Animals")
                                .document(selectedAnimalId)
                                .update(
                                        "name", name,
                                        "age", age,
                                        "breed", breed,
                                        "category", category,
                                        "reproductiveStatus", reproductiveStatus,
                                        "gender", gender,
                                        "weight", weight,
                                        "allergies", allergies,
                                        "base64Image", existingImage != null ? existingImage : ""
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MainActivity.this, "Animal modificat!", Toast.LENGTH_SHORT).show();
                                    resetFormAndReload();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Eroare la modificare!", Toast.LENGTH_SHORT).show();
                                    submitAnimalFormButton.setEnabled(true);
                                });
                    });
        } else {
            //select img noua
            String base64Image = compressAndResizeImage(imageUri);

            database.collection("users")
                    .document(uid)
                    .collection("Animals")
                    .document(selectedAnimalId)
                    .update(
                            "name", name,
                            "age", age,
                            "breed", breed,
                            "category", category,
                            "reproductiveStatus", reproductiveStatus,
                            "gender", gender,
                            "weight", weight,
                            "allergies", allergies,
                            "base64Image", base64Image != null ? base64Image : ""
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Animal modificat!", Toast.LENGTH_SHORT).show();
                        resetFormAndReload();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Eroare la modificare!", Toast.LENGTH_SHORT).show();
                        submitAnimalFormButton.setEnabled(true);
                    });
        }
    } else {
        //INSERT
        String animalID = database.collection("users").document(uid).collection("Animals").document().getId();
        String base64Image = imageUri != null ? compressAndResizeImage(imageUri) : "";


        animal newAnimal = new animal(name, breed, age);
        newAnimal.setCategory(category);
        newAnimal.setReproductiveStatus(reproductiveStatus);
        newAnimal.setGender(gender);
        newAnimal.setWeight(weight);
        newAnimal.setAllergies(allergies);
        newAnimal.setId(animalID);
        newAnimal.setBase64Image(base64Image);

        database.collection("users")
                .document(uid)
                .collection("Animals")
                .document(animalID)
                .set(newAnimal)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Animal adăugat!", Toast.LENGTH_SHORT).show();
                    resetFormAndReload();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Eroare la adăugare!", Toast.LENGTH_SHORT).show();
                    submitAnimalFormButton.setEnabled(true);
                });
    }
}

    private void resetFormAndReload() {
        animalNameEdit.setText("");
        animalBreedEdit.setText("");
        animalAgeEdit.setText("");
        animalImageView.setImageDrawable(null);
        imageUri = null;
        selectedAnimalId = null;
        addAnimalFormLayout.setVisibility(View.GONE);
        loadAnimals();
        submitAnimalFormButton.setEnabled(true);
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




    private void loadAnimals() {
        ProgressBar loadingSpinner = findViewById(R.id.loadingSpinner);
        loadingSpinner.setVisibility(View.VISIBLE);

        String uid =FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.collection("users").document(uid).collection("Animals")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        animals.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            animal animal = document.toObject(animal.class);
                            if (animal != null) {
                                animal.setId(document.getId());
                                if (animal.getAge() != null && !animal.getAge().isEmpty()) {
                                    String varstaCalculata = calculeazaVarstaDinData(animal.getAge());
                                    animal.setCalculatedAge(varstaCalculata);
                                }

                                if (animal.getBase64Image() != null) {
                                    byte[] decodedString = Base64.decode(animal.getBase64Image(), Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    animal.setBase64Image(animal.getBase64Image());
                                }
                                animals.add(animal);
                            }
                        }
                        animals_listAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "error loading animals", task.getException());
                        Toast.makeText(MainActivity.this, "Eroare la incarcarea animalelor!", Toast.LENGTH_SHORT).show();
                    }

                    loadingSpinner.setVisibility(View.GONE);
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