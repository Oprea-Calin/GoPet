package com.example.gopet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class MyPetSittingPostsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PetSittingPostAdapter adapter;
    private List<PetSittingPost> postList;
    private FirebaseFirestore db;
    private String currentUser;
    private Button btnAddPost;
    private ProgressBar progressBar;

    private int totalExpectedPosts = 0;
    private int loadedPosts = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_pet_sitting_posts, container, false);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView = view.findViewById(R.id.recyclerViewMyPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        adapter = new PetSittingPostAdapter(getContext(), postList, new PetSittingPostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(PetSittingPost post) {
                FirebaseFirestore.getInstance()
                        .collection("petSittingRequests")
                        .whereEqualTo("postId", post.id)
                        .get()
                        .addOnSuccessListener(requests -> {
                            boolean hasAccepted = false;

                            for (DocumentSnapshot doc : requests) {
                                String status = doc.getString("status");
                                if ("accepted".equals(status)) {
                                    hasAccepted = true;
                                    break;
                                }
                            }

                            Intent intent;

                            if (hasAccepted || requests.isEmpty()) {
                                intent = new Intent(getContext(), ViewOwnPostDetails.class);
                            } else {
                                intent = new Intent(getContext(), ViewRequestsActivity.class);
                            }

                            intent.putExtra("postId", post.id);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error checking requests", Toast.LENGTH_SHORT).show();
                        });
            }
            @Override
            public void onUsernameClick(PetSittingPost post) {
                FirebaseFirestore.getInstance()
                        .collection("petSittingRequests")
                        .whereEqualTo("postId", post.id)
                        .get()
                        .addOnSuccessListener(requests -> {
                            boolean hasAccepted = false;

                            for (DocumentSnapshot doc : requests) {
                                String status = doc.getString("status");
                                if ("accepted".equals(status)) {
                                    hasAccepted = true;
                                    break;
                                }
                            }

                            Intent intent;

                            if (hasAccepted || requests.isEmpty()) {
                                intent = new Intent(getContext(), ViewOwnPostDetails.class);
                            } else {
                                intent = new Intent(getContext(), ViewRequestsActivity.class);
                            }

                            intent.putExtra("postId", post.id);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error checking requests", Toast.LENGTH_SHORT).show();
                        });
            }
            @Override
            public void onDeleteClick(PetSittingPost post) {
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Delete post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            deletePostAndRequests(post);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }


        }, true);

        recyclerView.setAdapter(adapter);

        btnAddPost = view.findViewById(R.id.btnAddPost);
        btnAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreatePetSittingPostActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return view;
    }
    private void deletePostAndRequests(PetSittingPost post) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("petSittingRequests")
                .whereEqualTo("postId", post.id)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        db.collection("petSittingRequests").document(doc.getId()).delete();
                    }

                    db.collection("petSittingPosts").document(post.id)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                postList.remove(post);
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete related requests", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        postList.clear();
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);
        totalExpectedPosts = 0;
        loadedPosts = 0;

        loadMyPosts();
        loadAcceptedPosts();
    }

    private void loadMyPosts() {
        db.collection("petSittingPosts")
                .whereEqualTo("ownerId", currentUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    totalExpectedPosts += docs.size();

                    for (DocumentSnapshot doc : docs) {
                        PetSittingPost post = doc.toObject(PetSittingPost.class);
                        postList.add(post);
                        incrementAndCheckDone();
                    }

                    if (docs.isEmpty()) {
                        incrementAndCheckDone();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    incrementAndCheckDone();
                });
    }

    private void loadAcceptedPosts() {
        db.collection("petSittingRequests")
                .whereEqualTo("userId", currentUser)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(requests -> {
                    List<DocumentSnapshot> docs = requests.getDocuments();
                    totalExpectedPosts += docs.size();

                    if (docs.isEmpty()) {
                        incrementAndCheckDone();
                        return;
                    }

                    for (DocumentSnapshot reqDoc : docs) {
                        PetSittingRequest req = reqDoc.toObject(PetSittingRequest.class);
                        if (req != null) {
                            db.collection("petSittingPosts")
                                    .document(req.postId)
                                    .get()
                                    .addOnSuccessListener(postDoc -> {
                                        PetSittingPost post = postDoc.toObject(PetSittingPost.class);
                                        if (post != null && post.acceptedUserId != null) {
                                            db.collection("users")
                                                    .document(post.acceptedUserId)
                                                    .get()
                                                    .addOnSuccessListener(userDoc -> {
                                                        String name = userDoc.getString("username");
                                                        post.acceptedUsername = name;
                                                        postList.add(post);
                                                        incrementAndCheckDone();
                                                    })
                                                    .addOnFailureListener(e -> incrementAndCheckDone());
                                        } else {
                                            incrementAndCheckDone();
                                        }
                                    })
                                    .addOnFailureListener(e -> incrementAndCheckDone());
                        } else {
                            incrementAndCheckDone();
                        }
                    }
                })
                .addOnFailureListener(e -> incrementAndCheckDone());
    }

    private void incrementAndCheckDone() {
        loadedPosts++;
        if (loadedPosts >= totalExpectedPosts) {
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }
    }
}
