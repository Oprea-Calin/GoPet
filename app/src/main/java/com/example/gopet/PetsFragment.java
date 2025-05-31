package com.example.gopet;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gopet.*;
import com.example.gopet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

public class PetsFragment extends Fragment {

    private RecyclerView recyclerView;
    private animalsListAdapter adapter;
    private ArrayList<animal> animals = new ArrayList<>();
    private FirebaseFirestore db;
    private ProgressBar loadingSpinner;

    private View formLayout,addanimalButtonlayout;
    private TextView addtextview;
    private EditText animalName, animalBreed, animalAge, animalCategory, animalReproductiveStatus, animalGender, animalWeight, animalAllergies;
    private ImageView animalImageView, addAnimalBtn;
    private Button submitAnimalBtn, selectImageBtn;
    private Uri imageUri;
    private String selectedAnimalId;

    private boolean editing = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    animalImageView.setImageURI(imageUri);
                }
            }
    );
    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).showAddPetButton(true);

        ImageView addBtn = requireActivity().findViewById(R.id.addAnimalButtonGlobal);
        addBtn.setOnClickListener(v -> {
            resetForm();
            formLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            addBtn.setVisibility(View.GONE);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) requireActivity()).showAddPetButton(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pets, container, false);

        recyclerView = view.findViewById(R.id.recyclerPets);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
       // addAnimalBtn = view.findViewById(R.id.addAnimalButton);
        formLayout = view.findViewById(R.id.addAnimalFormLayout);
        animalImageView = view.findViewById(R.id.animalImageView);
        submitAnimalBtn = view.findViewById(R.id.submitAnimalBtn);
        selectImageBtn = view.findViewById(R.id.selectImageBtn);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        //addtextview = view.findViewById(R.id.addtextview);
        //addanimalButtonlayout = view.findViewById(R.id.addanimalButtonlayout);


        animalName = view.findViewById(R.id.animalName);
        animalBreed = view.findViewById(R.id.animalBreed);
        animalAge = view.findViewById(R.id.animalBirthDate);
        animalCategory = view.findViewById(R.id.animalCategory);
        animalReproductiveStatus = view.findViewById(R.id.animalReproductiveStatus);
        animalGender = view.findViewById(R.id.animalGender);
        animalWeight = view.findViewById(R.id.animalWeight);
        animalAllergies = view.findViewById(R.id.animalAllergies);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db = FirebaseFirestore.getInstance();

        adapter = new animalsListAdapter(animals, getContext(),
                a -> confirmDeleteAnimal(a),
                this::populateFormWithAnimal);

        recyclerView.setAdapter(adapter);
        formLayout.setVisibility(View.GONE);

        loadAnimals();

//        addAnimalBtn.setOnClickListener(v -> {
//            resetForm();
//            showForm(true);


//        });

        selectImageBtn.setOnClickListener(v -> openImageChooser());

        animalAge.setOnClickListener(v -> openDatePicker());

        submitAnimalBtn.setOnClickListener(v -> saveAnimal());

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(),
                (view, year, month, day) -> animalAge.setText(day + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void confirmDeleteAnimal(animal animal) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete pet")
                .setMessage("Are you sure you want to delete " + animal.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAnimal(animal))
                .setNegativeButton("No", null)
                .show();
    }

    private void resetForm() {
        animalName.setText("");
        animalBreed.setText("");
        animalAge.setText("");
        animalCategory.setText("");
        animalReproductiveStatus.setText("");
        animalGender.setText("");
        animalWeight.setText("");
        animalAllergies.setText("");
        animalImageView.setImageDrawable(null);
        imageUri = null;
        selectedAnimalId = null;
        editing = false;
    }

    private void populateFormWithAnimal(animal a) {
        editing = true;
        selectedAnimalId = a.getId();
        animalName.setText(a.getName());
        animalBreed.setText(a.getBreed());
        animalAge.setText(a.getAge());
        animalCategory.setText(a.getCategory());
        animalReproductiveStatus.setText(a.getReproductiveStatus());
        animalGender.setText(a.getGender());
        animalWeight.setText(a.getWeight() != null ? a.getWeight().toString() : "");
        animalAllergies.setText(a.getAllergies());
        if (a.getBase64Image() != null && !a.getBase64Image().isEmpty()) {
            byte[] decoded = Base64.decode(a.getBase64Image(), Base64.DEFAULT);
            animalImageView.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
        }
        showForm(true);
    }
    private void showForm(boolean show) {
        formLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showAddPetButton(false);
        }
       // addanimalButtonlayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void saveAnimal() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String name = animalName.getText().toString();
        String breed = animalBreed.getText().toString();
        String age = animalAge.getText().toString();
        String category = animalCategory.getText().toString();
        String reproductiveStatus = animalReproductiveStatus.getText().toString();
        String gender = animalGender.getText().toString();
        String allergies = animalAllergies.getText().toString();
        Float weight = animalWeight.getText().toString().isEmpty() ? null : Float.parseFloat(animalWeight.getText().toString());
        //String base64Image = imageUri != null ? compressAndResizeImage(imageUri) : "";

        String base64Image="";
        if (imageUri != null) {
            base64Image = compressAndResizeImage(imageUri);
        } else if (editing && selectedAnimalId != null) {
            for (animal aOld : animals) {
                if (aOld.getId().equals(selectedAnimalId)) {
                    base64Image = aOld.getBase64Image();
                    break;
                }
            }
        } else {
            base64Image = "";
        }



        if (name.isEmpty() || age.isEmpty() || breed.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        animal a = new animal(name, breed, age);
        a.setCategory(category);
        a.setReproductiveStatus(reproductiveStatus);
        a.setGender(gender);
        a.setAllergies(allergies);
        a.setWeight(weight);
        a.setBase64Image(base64Image);

        if (editing) {
            db.collection("users").document(uid).collection("Animals").document(selectedAnimalId)
                    .set(a)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Pet updated", Toast.LENGTH_SHORT).show();
                        showForm(false);
                        loadAnimals();
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).showAddPetButton(true);
                        }
                    });
        } else {
            String id = db.collection("users").document(uid).collection("Animals").document().getId();
            a.setId(id);
            db.collection("users").document(uid).collection("Animals").document(id)
                    .set(a)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Pet added", Toast.LENGTH_SHORT).show();
                        showForm(false);
                        loadAnimals();
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).showAddPetButton(true);
                        }
                    });
        }
    }

    private void deleteAnimal(animal a) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).collection("Animals").document(a.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Pet deleted", Toast.LENGTH_SHORT).show();
                    loadAnimals();
                });
    }

    private void loadAnimals() {
        loadingSpinner.setVisibility(View.VISIBLE);
        animals.clear();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("Animals")
                .get()
                .addOnSuccessListener(task -> {
                    for (DocumentSnapshot document : task.getDocuments()) {
                        animal animal = document.toObject(animal.class);
                        if (animal != null) {
                            animal.setId(document.getId());
                            if (animal.getAge() != null && !animal.getAge().isEmpty()) {
                                String varstaCalculata = calculeazaVarstaDinData(animal.getAge());
                                animal.setCalculatedAge(varstaCalculata);
                            }
                            animal.setShared(false);
                            animals.add(animal);
                        }
                    }

                    db.collection("users").document(uid).collection("sharedAnimalsRequests")
                            .whereEqualTo("status", "accepted")
                            .get()
                            .addOnSuccessListener(requestsTask -> {
                                List<DocumentSnapshot> shareRequests = requestsTask.getDocuments();

                                if (shareRequests.isEmpty()) {
                                    adapter.notifyDataSetChanged();
                                    loadingSpinner.setVisibility(View.GONE);
                                    return;
                                }

                                final int[] finishedCount = {0};
                                for (DocumentSnapshot requestDoc : shareRequests) {
                                    String fromUserId = requestDoc.getId();

                                    db.collection("users").document(fromUserId)
                                            .get()
                                            .addOnSuccessListener(userDoc -> {
                                                String fromUsername = userDoc.getString("username");

                                                db.collection("users").document(fromUserId).collection("Animals")
                                                        .get()
                                                        .addOnSuccessListener(sharedAnimalsTask -> {
                                                            for (DocumentSnapshot sharedAnimalDoc : sharedAnimalsTask.getDocuments()) {
                                                                animal sharedAnimal = sharedAnimalDoc.toObject(animal.class);
                                                                if (sharedAnimal != null) {
                                                                    sharedAnimal.setId(sharedAnimalDoc.getId());
                                                                    if (sharedAnimal.getAge() != null && !sharedAnimal.getAge().isEmpty()) {
                                                                        String varstaCalculata = calculeazaVarstaDinData(sharedAnimal.getAge());
                                                                        sharedAnimal.setCalculatedAge(varstaCalculata);
                                                                    }
                                                                    sharedAnimal.setShared(true);
                                                                    sharedAnimal.setSharedFromUsername(fromUsername);
                                                                    animals.add(sharedAnimal);
                                                                }
                                                            }

                                                            finishedCount[0]++;
                                                            if (finishedCount[0] == shareRequests.size()) {
                                                                adapter.notifyDataSetChanged();
                                                                loadingSpinner.setVisibility(View.GONE);
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            finishedCount[0]++;
                                                            if (finishedCount[0] == shareRequests.size()) {
                                                                adapter.notifyDataSetChanged();
                                                                loadingSpinner.setVisibility(View.GONE);
                                                            }
                                                        });
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error loading shared animals", Toast.LENGTH_SHORT).show();
                                adapter.notifyDataSetChanged();
                                loadingSpinner.setVisibility(View.GONE);
                            });

                })
                .addOnFailureListener(e -> {
                    loadingSpinner.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading your animals", Toast.LENGTH_SHORT).show();
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
    private String compressAndResizeImage(Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(inputStream);
            Bitmap resized = Bitmap.createScaledBitmap(original, 800, 800, true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
