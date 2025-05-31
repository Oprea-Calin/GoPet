package com.example.gopet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView logoutImage, viewProfile, profileImageView, addAnimalButtonGlobal;
    EditText usernameEdit, quoteEdit;
    Button selectProfileImage, submitProfileUpdateButton;
    RecyclerView profileRecyclerView;
    NestedScrollView addUserFormLayout;
    TextView commentsHeader;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    FirebaseAuth auth;
    ProfileAdapter profileAdapter;
    View fragmentContainer;
    Fragment currentFragment;
    Uri profileImageUri = null;
    static final int PICK_IMAGE_REQUEST = 1001;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private NestedScrollView profileContainer;

    boolean isProfileVisible = false;
    boolean isEditFormVisible = false;
    String existingBase64Image;
    TextView usernameLabel, quoteLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addAnimalButtonGlobal = findViewById(R.id.addAnimalButtonGlobal);

        profileContainer = findViewById(R.id.profileContainer);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList);
        commentsRecyclerView.setAdapter(commentAdapter);
        commentsHeader = findViewById(R.id.commentsHeader);

        logoutImage = findViewById(R.id.logoutImage);
        viewProfile = findViewById(R.id.viewProfile);
        profileImageView = findViewById(R.id.profileImageView);
        usernameEdit = findViewById(R.id.usernameEdit);
        quoteEdit = findViewById(R.id.quoteEdit);
        selectProfileImage = findViewById(R.id.selectProfileImage);
        submitProfileUpdateButton = findViewById(R.id.submitProfileUpdateButton);
        selectProfileImage.setOnClickListener(v -> openImageChooser());

        submitProfileUpdateButton.setOnClickListener(v -> updateProfile());

        profileRecyclerView = findViewById(R.id.profileRecycleView);
        addUserFormLayout = findViewById(R.id.addProfileFormLayout);
        fragmentContainer = findViewById(R.id.fragment_container);

        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_pets) {
                selectedFragment = new PetsFragment();
            } else if (id == R.id.nav_social) {
                selectedFragment = new SocialFragment();
            } else if (id == R.id.nav_pet_sitting) {
                startActivity(new Intent(this, PetSittingDashboardActivity.class));
                return true;
            }

            if (selectedFragment != null) {
                resetViewVisibility();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                fragmentContainer.setVisibility(View.VISIBLE);
            }
            return true;
        });

        usernameLabel = findViewById(R.id.usernameLabel);
        quoteLabel = findViewById(R.id.quoteLabel);

        if (!quoteEdit.getText().toString().isEmpty()) {
            quoteLabel.setVisibility(View.VISIBLE);
        }
        quoteEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    quoteLabel.setVisibility(View.VISIBLE);
                } else {
                    quoteLabel.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        usernameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    usernameLabel.setVisibility(View.VISIBLE);
                } else {
                    usernameLabel.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });




        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_pets);
        }

        logoutImage.setOnClickListener(v -> logout());

        viewProfile.setOnClickListener(v -> {
            if (isProfileVisible) {

                hideProfileViews();
                fragmentContainer.setVisibility(View.VISIBLE);
            } else {

                currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                fragmentContainer.setVisibility(View.GONE);
                profileContainer.setVisibility(View.VISIBLE);
                profileRecyclerView.setVisibility(View.VISIBLE);
                commentsRecyclerView.setVisibility(View.VISIBLE);
                commentsHeader.setVisibility(View.VISIBLE);
                addAnimalButtonGlobal.setVisibility(View.GONE);
                loadUserProfile();
                loadComments();
                isProfileVisible = true;
                isEditFormVisible = false;
            }
        });

    }
    private void resetViewVisibility() {
        profileContainer.setVisibility(View.GONE);
        addUserFormLayout.setVisibility(View.GONE);
        commentsHeader.setVisibility(View.GONE);
        isProfileVisible = false;
        isEditFormVisible = false;
    }
    private void loadComments() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("comments")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            commentList.add(comment);
                        }
                    }
                    commentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                });
    }
    private void hideProfileViews() {
        resetViewVisibility();
        fragmentContainer.setVisibility(View.VISIBLE);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        editor.clear().apply();
        startActivity(new Intent(this, Login.class));
        finish();
    }

    private void loadUserProfile() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = document.getString("username");
                    String quote = document.getString("quote");
                    String base64Image = document.getString("base64Image");
                    existingBase64Image = base64Image;

                    if (base64Image != null && !base64Image.isEmpty()) {
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        Glide.with(this)
                                .asBitmap()
                                .load(decodedByte)
                                .circleCrop()
                                .into(profileImageView);

                    }

                    Profile profile = new Profile(username, quote, base64Image);
                    List<Profile> profileList = new ArrayList<>();
                    profileList.add(profile);
                    setUpProfileRecyclerView(profileList);

                    usernameEdit.setText(username);
                    quoteEdit.setText(quote);
                }
            } else {
                Toast.makeText(MainActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpProfileRecyclerView(List<Profile> profileList) {
        profileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        profileAdapter = new ProfileAdapter(profileList, profile -> {
            profileContainer.setVisibility(View.GONE);
            commentsHeader.setVisibility(View.GONE);
            addUserFormLayout.setVisibility(View.VISIBLE);
            isEditFormVisible = true;
        },false);
        profileRecyclerView.setAdapter(profileAdapter);
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
            profileImageUri = data.getData();
            profileImageView.setImageURI(profileImageUri);
        }
    }
    private void updateProfile() {
        String newUsername = usernameEdit.getText().toString().trim();
        String newQuote = quoteEdit.getText().toString().trim();

        if (newUsername.isEmpty() || newQuote.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        String base64Image;
        if (profileImageUri != null) {
            base64Image = compressAndResizeImage(profileImageUri);
        } else {
            base64Image = existingBase64Image != null ? existingBase64Image : "";
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("username", newUsername, "quote", newQuote, "base64Image", base64Image)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    hideProfileViews();
                    fragmentContainer.setVisibility(View.VISIBLE);
                    addAnimalButtonGlobal.setVisibility(View.VISIBLE);
                    loadUserProfile();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show());
    }
    public void showAddPetButton(boolean show) {
        if(addAnimalButtonGlobal != null)
            addAnimalButtonGlobal.setVisibility(show ? View.VISIBLE : View.GONE);
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
}


