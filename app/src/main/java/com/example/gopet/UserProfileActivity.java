package com.example.gopet;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";

    private ImageView userProfileImage;
    private TextView userProfileName;
    private EditText commentInput;
    private Button btnPostComment;
    private RecyclerView commentsRecycler;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();

    private String targetUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initializeViews();
        initializeFirebase();
        checkTargetUserId();
    }

    private void initializeViews() {
        userProfileImage = findViewById(R.id.imageProfile);
        userProfileName = findViewById(R.id.textUsername);
        commentInput = findViewById(R.id.editTextComment);
        btnPostComment = findViewById(R.id.buttonSendComment);
        commentsRecycler = findViewById(R.id.commentsRecyclerView);

        // Initialize RecyclerView
        adapter = new CommentAdapter(commentList);
        commentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        commentsRecycler.setAdapter(adapter);
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void checkTargetUserId() {
        targetUserId = getIntent().getStringExtra("userId"); // Changed from targetUserId to userId

        if (targetUserId == null || targetUserId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No userId provided in intent");
            finish();
            return;
        }

        loadUserProfile();
        setupCommentButton();
    }

    private void loadUserProfile() {
        db.collection("users")
                .document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String base64Image = documentSnapshot.getString("base64Image");

                        if (username != null) {
                            userProfileName.setText(username);
                        } else {
                            userProfileName.setText("Unknown User");
                        }

                        if (base64Image != null && !base64Image.isEmpty()) {
                            try {
                                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                                userProfileImage.setImageBitmap(
                                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length)
                                );
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "Error decoding base64 image", e);
                            }
                        }

                        loadComments();
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "User document doesn't exist");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading user profile", e);
                    finish();
                });
    }

    private void setupCommentButton() {
        btnPostComment.setOnClickListener(v -> {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                postComment();
            } else {
                Toast.makeText(this, "You must be logged in to comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment() {
        String content = commentInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorId = currentUser.getUid();

        db.collection("users").document(authorId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        String image = doc.getString("base64Image");

                        String timestamp = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                                .format(new Date());

                        Comment comment = new Comment(authorId,
                                username != null ? username : "Anonymous",
                                image,
                                content,
                                timestamp);

                        db.collection("users")
                                .document(targetUserId)
                                .collection("comments")
                                .add(comment)
                                .addOnSuccessListener(aVoid -> {
                                    commentInput.setText("");
                                    loadComments();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error posting comment", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error verifying user", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting user data", e);
                });
    }

    private void loadComments() {
        if (targetUserId == null) {
            return;
        }

        db.collection("users")
                .document(targetUserId)
                .collection("comments")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(query -> {
                    commentList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            commentList.add(comment);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading comments", e);
                });
    }
}