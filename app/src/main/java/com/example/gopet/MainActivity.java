package com.example.gopet;

import android.animation.ValueAnimator;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    ImageView logoutImage, settingsImage;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    RecyclerView animalsView, usersRecyclerView, profileRecycleView;
    UserAdapter userAdapter;
    ProfileAdapter profileAdapter;
    List<DocumentSnapshot> usersList,friendsList;
    Button btnAllUsers;
    NestedScrollView addAnimalFormLayout, addUserFormLayout;
    FirebaseFirestore database;
    animalsListAdapter animals_listAdapter;
    ArrayList<animal> animals;
    EditText animalNameEdit, animalAgeEdit, animalBreedEdit;
    EditText usernameEdit, quoteEdit;
    ImageView profileImageView;
    Button submitProfileUpdateButton,selectProfileImage, sendFriendRequest;
    Uri profileImageUri;
    EditText animalCategoryEdit, animalReproductiveStatusEdit, animalGenderEdit, animalWeightEdit, animalAllergiesEdit;
    Button submitAnimalFormButton, btnFriends;
    ImageView animalImageView, addAnimal, viewProfile;
    String existingBase64Image;
    boolean isProfileImageSelected;
    static final int PICK_IMAGE_REQUEST = 1;

    Uri imageUri;
    boolean profileShown = false;

    String selectedAnimalId = null;
    final boolean[] isExpandedMyAnimals = {false};
    TextView myAnimalsTitle;

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

        btnFriends = findViewById(R.id.btnFriends);
        btnFriends.setOnClickListener(view -> {
            showFriends();
        });
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        btnAllUsers = findViewById(R.id.btnAllUsers);
        usersList = new ArrayList<>();
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsList = new ArrayList<>();
        userAdapter = new UserAdapter(usersList, friendsList, new UserAdapter.OnAddFriendClickListener() {
            @Override
            public void onAddFriendClicked(DocumentSnapshot user) {
                addFriend(user);
            }

            @Override
            public void onShareAnimalsClicked(DocumentSnapshot user) {
                shareAnimalsWithFriend(user);
            }
        });
        userAdapter.setOnReloadAnimalsListener(() -> {
            loadAnimals();
        });
        loadFriends();
        addUserFormLayout = findViewById(R.id.addProfileFormLayout);
        userAdapter.setOnRemoveFriendClickListener(user -> {
            removeFriend(user);
        });


        usersRecyclerView.setAdapter(userAdapter);

        usersRecyclerView.setVisibility(View.GONE);
        btnAllUsers.setOnClickListener(view -> {
            addUserFormLayout.setVisibility(View.GONE);

            if(usersRecyclerView.getVisibility() == View.VISIBLE)
            {
                usersRecyclerView.setVisibility(View.GONE);
                animateAnimalRecyclerViewHeight(200, 600);
                isExpandedMyAnimals[0] = true;
            }
            else{
                if(profileShown==true)
                {
                    profileRecycleView.setVisibility(View.GONE);
                    profileShown = false;
                }
                if(addAnimalFormLayout.getVisibility() == View.VISIBLE)
                {
                    addAnimalFormLayout.setVisibility(View.GONE);
                }
                if(isExpandedMyAnimals[0]==true)
                {
                    animateAnimalRecyclerViewHeight(600,200);
                    isExpandedMyAnimals[0] = false;
                }
                loadUsers();
                usersRecyclerView.setVisibility(View.VISIBLE);

            }
        });

        profileRecycleView = findViewById(R.id.profileRecycleView);
        viewProfile = findViewById(R.id.viewProfile);

        usernameEdit = findViewById(R.id.usernameEdit);
        quoteEdit =findViewById(R.id.quoteEdit);
        profileImageView =findViewById(R.id.profileImageView);
        selectProfileImage =findViewById(R.id.selectProfileImage);
        submitProfileUpdateButton = findViewById(R.id.submitProfileUpdateButton);

        selectProfileImage.setOnClickListener(v -> {
            isProfileImageSelected = true;
            openImageChooser();
        });
        submitProfileUpdateButton.setOnClickListener(v -> updateProfile());

        myAnimalsTitle = findViewById(R.id.myAnimalsTitle);
        animalsView = findViewById(R.id.animalsView);
        animalImageView = findViewById(R.id.animalImageView);
        Button selectImageButton = findViewById(R.id.addImageButton);
        selectImageButton.setOnClickListener(v -> {
            isProfileImageSelected = false;
            openImageChooser();});

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
                    usersRecyclerView.setVisibility(View.GONE);
                    if(profileShown == true){
                        profileRecycleView.setVisibility(View.GONE);
                        profileShown=false;
                    }
                    addAnimalFormLayout.setVisibility(View.VISIBLE);
                    addUserFormLayout.setVisibility(View.GONE);
                    profileShown = false;
                    //myAnimalsTitle.setEnabled(false);
                    if (isExpandedMyAnimals[0]) {
                        animateAnimalRecyclerViewHeight(600, 200);
                        isExpandedMyAnimals[0] = false;
                    }
                    populateFormWithAnimal(animal);
                }
        );
        animalsView.setAdapter(animals_listAdapter);

        animalAgeEdit = findViewById(R.id.animalBirthDate);
        addAnimalFormLayout = findViewById(R.id.addAnimalFormScrollView);
        animalBreedEdit = findViewById(R.id.animalBreed);
        animalNameEdit = findViewById(R.id.animalName);
        submitAnimalFormButton = findViewById(R.id.addAnimalFormButton);

        logoutImage = findViewById(R.id.logoutImage);
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        auth = FirebaseAuth.getInstance();
        boolean rememberMe = sharedPreferences.getBoolean("remember", false);

        addAnimal = findViewById(R.id.addAnimalButton);
        addAnimal.setOnClickListener(v -> {

            addUserFormLayout.setVisibility(View.GONE);
            usersRecyclerView.setVisibility(View.GONE);
            if (addAnimalFormLayout.getVisibility() == View.VISIBLE) {

                addAnimalFormLayout.setVisibility(View.GONE);
                animateAnimalRecyclerViewHeight(200, 600);
                isExpandedMyAnimals[0] = true;
            } else {
                profileRecycleView.setVisibility(View.GONE);
                profileShown = false;
                addAnimalFormLayout.setVisibility(View.VISIBLE);
                usersRecyclerView.setVisibility(View.GONE);
                if(animalsView.getVisibility() == View.VISIBLE)
                {
                    if (isExpandedMyAnimals[0]) {
                    animateAnimalRecyclerViewHeight(600, 200);
                    isExpandedMyAnimals[0] = false;
                }
                }
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
        RecyclerView finalAnimalsView = animalsView;
        myAnimalsTitle.setOnClickListener(v -> {

            if (addAnimalFormLayout.getVisibility() == View.VISIBLE) {

                addUserFormLayout.setVisibility(View.GONE);
                profileRecycleView.setVisibility(View.GONE);
                profileShown = false;
                addAnimalFormLayout.setVisibility(View.GONE);
                myAnimalsTitle.setEnabled(true);

                if (!isExpandedMyAnimals[0]) {
                    animateAnimalRecyclerViewHeight(200, 600);
                    isExpandedMyAnimals[0] = true;
                }
                if(profileShown == true){
                    profileRecycleView.setVisibility(View.GONE);
                    profileShown=false;
                }
            } else {
                addUserFormLayout.setVisibility(View.GONE);
                profileRecycleView.setVisibility(View.GONE);
                profileShown = false;
                if(isExpandedMyAnimals[0])
                {
                    animateAnimalRecyclerViewHeight(600,200);
                    isExpandedMyAnimals[0] = false;
                }
                else{
                    animateAnimalRecyclerViewHeight(200,600);
                    isExpandedMyAnimals[0] = true;
                }
                //isExpandedMyAnimals[0] = !isExpandedMyAnimals[0];
                if(isExpandedMyAnimals[0])
                {
                    usersRecyclerView.setVisibility(View.GONE);
                }
            }
        });
        viewProfile.setOnClickListener(view -> {



            if(profileShown == false) {
                usersRecyclerView.setVisibility(View.GONE);
                addAnimalFormLayout.setVisibility(View.GONE);
                profileRecycleView.setVisibility(View.VISIBLE);
                addUserFormLayout.setVisibility(View.GONE);
                if(isExpandedMyAnimals[0] == true)
                {
                    animateAnimalRecyclerViewHeight(600,200);
                    isExpandedMyAnimals[0]= false;
                }

                loadUserProfile();
                profileShown = true;
            }
            else{
                profileRecycleView.setVisibility(View.GONE);
                addUserFormLayout.setVisibility(View.GONE);


                profileShown=false;
                animateAnimalRecyclerViewHeight(200,600);
                isExpandedMyAnimals[0]= true;

            }

        });
        submitAnimalFormButton.setOnClickListener(v -> {

            String name = animalNameEdit.getText().toString();
            String age = animalAgeEdit.getText().toString();
            String breed = animalBreedEdit.getText().toString();
            if(name.isEmpty() || age.isEmpty() || breed.isEmpty())
            {
                Toast.makeText(this, "Completați toate detaliile!", Toast.LENGTH_SHORT).show();
            }
            else{
                submitAnimalFormButton.setEnabled(false);
                saveAnimaltoDatabase(name, age, breed);
            }
        });
        animateAnimalRecyclerViewHeight(200, 600);
        isExpandedMyAnimals[0] = true;

        loadAnimals();
        logoutImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                logout();
            }

        });
    }
    private void removeFriend(DocumentSnapshot user) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String friendId = user.getId();

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Șterge din ambele liste
        database.collection("users")
                .document(uid)
                .collection("friends")
                .document(friendId)
                .delete();

        database.collection("users")
                .document(friendId)
                .collection("friends")
                .document(uid)
                .delete();

        // Elimină din lista locală și notifică adapterul
        friendsList.removeIf(f -> f.getId().equals(friendId));
        usersList.removeIf(u -> u.getId().equals(friendId)); // doar dacă e listă de prieteni

        userAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Prieten eliminat!", Toast.LENGTH_SHORT).show();
    }

    private void showFriends() {
        addUserFormLayout.setVisibility(View.GONE);
        profileRecycleView.setVisibility(View.GONE);
        addAnimalFormLayout.setVisibility(View.GONE);
        profileShown = false;

        if (usersRecyclerView.getVisibility() == View.VISIBLE) {
            usersRecyclerView.setVisibility(View.GONE);
            if (!isExpandedMyAnimals[0]) {
                animateAnimalRecyclerViewHeight(200, 600);
                isExpandedMyAnimals[0] = true;
            }
        } else {
            if (isExpandedMyAnimals[0]) {
                animateAnimalRecyclerViewHeight(600, 200);
                isExpandedMyAnimals[0] = false;
            }

            usersList.clear();
            userAdapter.notifyDataSetChanged();
            usersRecyclerView.setVisibility(View.VISIBLE);

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("friends")
                    .whereEqualTo("status", "confirmed")
                    .get()
                    .addOnSuccessListener(friendDocs -> {
                        if (!friendDocs.isEmpty()) {
                            List<DocumentSnapshot> confirmedFriends = new ArrayList<>();

                            for (DocumentSnapshot doc : friendDocs) {
                                String friendId = doc.getId();
                                FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(friendId)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {
                                            confirmedFriends.add(userDoc);

                                            if (confirmedFriends.size() == friendDocs.size()) {
                                                usersList.clear();
                                                usersList.addAll(confirmedFriends);
                                                userAdapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Nu ai prieteni confirmați.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Eroare la încărcarea prietenilor.", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void updateProfile() {
        String newUsername = usernameEdit.getText().toString();
        String newQuote = quoteEdit.getText().toString();

        if (newUsername.isEmpty() || newQuote.isEmpty()) {
            Toast.makeText(this, "Completați toate detaliile!", Toast.LENGTH_SHORT).show();
            return;
        }

        //String base64Image = profileImageUri != null ? compressAndResizeImage(profileImageUri) : "";
        String base64Image="";
        if(profileImageUri  != null)
            base64Image = compressAndResizeImage(profileImageUri);
        else{
            if(existingBase64Image != null)
                base64Image = existingBase64Image;
        }



        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").document(uid)
                .update(
                        "username", newUsername,
                        "quote", newQuote,
                        "base64Image", base64Image
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    loadUserProfile();
                    addUserFormLayout.setVisibility(View.GONE);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                });
    }
    private void shareAnimalsWithFriend(DocumentSnapshot friendUser) {
        String friendId = friendUser.getId();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection("users")
                .document(friendId)
                .collection("sharedAnimalsRequests")
                .document(currentUserId)
                .set(new SharedAnimalsRequest(currentUserId, "pending"))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Solicitare de partajare trimisă!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Eroare la trimiterea solicitării!", Toast.LENGTH_SHORT).show();
                });
    }

    private void addFriend(DocumentSnapshot user) {
        if (!friendsList.contains(user)) {
            friendsList.add(user);
            saveFriendToDatabase(user.getId());
            Toast.makeText(this, user.getString("username") + " adăugat la prieteni!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Deja este în lista ta de prieteni.", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveFriendToDatabase(String friendId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore database = FirebaseFirestore.getInstance();


        database.collection("users")
                .document(friendId)
                .collection("friends")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && "pending".equals(documentSnapshot.getString("status"))) {
                        database.collection("users")
                                .document(uid)
                                .collection("friends")
                                .document(friendId)
                                .set(new Friend(friendId, "confirmed"));

                        database.collection("users")
                                .document(friendId)
                                .collection("friends")
                                .document(uid)
                                .update("status", "confirmed");

                        Toast.makeText(MainActivity.this, "Acum sunteți prieteni!", Toast.LENGTH_SHORT).show();
                        loadFriends();

                    } else {
                        //pending
                        Friend friend = new Friend(friendId, "pending");
                        database.collection("users")
                                .document(uid)
                                .collection("friends")
                                .document(friendId)
                                .set(friend)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MainActivity.this, "Cerere trimisă!", Toast.LENGTH_SHORT).show();
                                    loadFriends();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Eroare la trimiterea cererii!", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    private void loadFriends() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users")
                .document(uid)
                .collection("friends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        friendsList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            friendsList.add(document);
                        }
                        userAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "Eroare la încărcarea prietenilor!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserProfile() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String username = document.getString("username");
                            String quote = document.getString("quote");
                            String base64Image = document.getString("base64Image");
                            existingBase64Image = document.getString("base64Image");
                            if (base64Image != null && !base64Image.isEmpty()) {
                                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                profileImageView.setImageBitmap(decodedByte);
                            }
                            Profile profile = new Profile(username, quote, base64Image);

                            List<Profile> profileList = new ArrayList<>();
                            profileList.add(profile);
                            setUpProfileRecyclerView(profileList);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUpProfileRecyclerView(List<Profile> profileData) {
        RecyclerView profileRecyclerView = findViewById(R.id.profileRecycleView);
        profileRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        profileAdapter = new ProfileAdapter(profileData, profile -> {

            
            if (profileShown) {
                profileRecycleView.setVisibility(View.GONE);
                profileShown = false;
            }

            addUserFormLayout.setVisibility(View.VISIBLE);
            usernameEdit.setText(profile.getUsername());
            quoteEdit.setText(profile.getQuote());

            if (profile.getBase64Image() != null && !profile.getBase64Image().isEmpty()) {
                byte[] decodedString = Base64.decode(profile.getBase64Image(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                profileImageView.setImageBitmap(decodedByte);
            } else {
            }
        });

        profileRecyclerView.setAdapter(profileAdapter);
    }

    private void animateRecyclerViewHeight(View recyclerView, int startHeightDp, int endHeightDp) {
        int startHeight = dpToPx(startHeightDp);
        int endHeight = dpToPx(endHeightDp);

        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            recyclerView.setLayoutParams(params);
        });
        animator.start();
    }
    private void animateAnimalRecyclerViewHeight(int startHeightDp, int endHeightDp) {
        int startHeight = dpToPx(startHeightDp);
        int endHeight = dpToPx(endHeightDp);

        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = animalsView.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            animalsView.setLayoutParams(params);
        });
        animator.start();
    }
    private void loadUsers() {
        database.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        usersList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            usersList.add(document);
                        }
                        userAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "Error loading users", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
        if (isExpandedMyAnimals[0]) {
            animateAnimalRecyclerViewHeight(animalsView.getHeight(), dpToPx(200));
            isExpandedMyAnimals[0] = false;
        }
        animalNameEdit.setText(animal.getName());
        animalBreedEdit.setText(animal.getBreed());
        animalAgeEdit.setText(animal.getAge());
        selectedAnimalId = animal.getId();
        animalCategoryEdit.setText(animal.getCategory());
        animalReproductiveStatusEdit.setText(animal.getReproductiveStatus());
        animalGenderEdit.setText(animal.getGender());
        animalWeightEdit.setText(animal.getWeight() != null ? String.valueOf(animal.getWeight()) : "");
        animalAllergiesEdit.setText(animal.getAllergies());
        if (animal.getBase64Image() != null && !animal.getBase64Image().isEmpty()) {
            byte[] decodedString = Base64.decode(animal.getBase64Image(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            animalImageView.setImageBitmap(decodedByte);
        } else {
            animalImageView.setImageDrawable(null);
        }


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
            if(isProfileImageSelected == true)
            {
                profileImageUri = data.getData();
                profileImageView.setImageURI(profileImageUri);

            }
            else{
                imageUri = data.getData();
                animalImageView.setImageURI(imageUri);
            }

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
        animalCategoryEdit.setText("");
        animalReproductiveStatusEdit.setText("");
        animalGenderEdit.setText("");
        animalWeightEdit.setText("");
        animalAllergiesEdit.setText("");
        animalImageView.setImageDrawable(null);
        imageUri = null;
        selectedAnimalId = null;

        //addAnimalFormLayout.setVisibility(View.GONE);
        //animateRecyclerViewHeight(600, 200);
        loadAnimals();
         submitAnimalFormButton.setEnabled(true);


        if (addAnimalFormLayout.getVisibility() == View.VISIBLE) {
            addAnimalFormLayout.setVisibility(View.GONE);
            myAnimalsTitle.setEnabled(true);
            animateAnimalRecyclerViewHeight(200, 600);
            isExpandedMyAnimals[0] = true;
        } else {
            addAnimalFormLayout.setVisibility(View.VISIBLE);
            myAnimalsTitle.setEnabled(false);
            if (isExpandedMyAnimals[0]) {
                animateAnimalRecyclerViewHeight(600, 200);
                isExpandedMyAnimals[0] = false;
            }
        }
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

        animals.clear();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseFirestore.getInstance();

        database.collection("users").document(uid).collection("Animals")
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


                    database.collection("users").document(uid).collection("sharedAnimalsRequests")
                            .whereEqualTo("status", "accepted")
                            .get()
                            .addOnSuccessListener(requestsTask -> {
                                List<DocumentSnapshot> shareRequests = requestsTask.getDocuments();

                                if (!shareRequests.isEmpty()) {
                                    for (DocumentSnapshot requestDoc : shareRequests) {
                                        String fromUserId = requestDoc.getId();

                                        database.collection("users").document(fromUserId)
                                                .get()
                                                .addOnSuccessListener(userDoc -> {
                                                    String fromUsername = userDoc.getString("username");

                                                    database.collection("users").document(fromUserId).collection("Animals")
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
                                                                animals_listAdapter.notifyDataSetChanged();
                                                            });
                                                });
                                    }
                                } else {
                                    animals_listAdapter.notifyDataSetChanged();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Eroare la încărcarea cererilor de partajare", Toast.LENGTH_SHORT).show();
                            });

                    loadingSpinner.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    loadingSpinner.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Eroare la încărcarea animalelor!", Toast.LENGTH_SHORT).show();
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
    }
}
