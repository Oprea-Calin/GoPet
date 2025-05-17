package com.example.gopet;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class SocialFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<DocumentSnapshot> usersList, friendsList;
    private FirebaseFirestore db;
    private Button btnAllUsers, btnFriends;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social, container, false);

        recyclerView = view.findViewById(R.id.recyclerSocial);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAllUsers = view.findViewById(R.id.btnAllUsers);
        btnFriends = view.findViewById(R.id.btnFriends);

        db = FirebaseFirestore.getInstance();
        usersList = new ArrayList<>();
        friendsList = new ArrayList<>();

        userAdapter = new UserAdapter(usersList, friendsList, new UserAdapter.OnAddFriendClickListener() {
            @Override
            public void onAddFriendClicked(DocumentSnapshot user) {
                addFriend(user);
            }

            @Override
            public void onShareAnimalsClicked(DocumentSnapshot user) {
                shareAnimals(user);
            }
        });

        recyclerView.setAdapter(userAdapter);
        loadFriends();

        btnAllUsers.setOnClickListener(v -> loadAllUsers());
        btnFriends.setOnClickListener(v -> showFriends());

        return view;
    }

    private void loadAllUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    usersList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        usersList.add(doc);
                    }
                    userAdapter.notifyDataSetChanged();
                });
    }

    private void showFriends() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid).collection("friends")
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(friendDocs -> {
                    List<DocumentSnapshot> confirmedFriends = new ArrayList<>();

                    for (DocumentSnapshot doc : friendDocs) {
                        String friendId = doc.getId();
                        db.collection("users").document(friendId)
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
                });
    }

    private void loadFriends() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).collection("friends")
                .get()
                .addOnSuccessListener(docs -> {
                    friendsList.clear();
                    friendsList.addAll(docs.getDocuments());
                });
    }

    private void addFriend(DocumentSnapshot user) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String friendId = user.getId();

        db.collection("users")
                .document(friendId)
                .collection("friends")
                .document(currentUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && "pending".equals(doc.getString("status"))) {
                        db.collection("users").document(currentUid)
                                .collection("friends").document(friendId)
                                .set(new Friend(friendId, "confirmed"));

                        db.collection("users").document(friendId)
                                .collection("friends").document(currentUid)
                                .update("status", "confirmed");
                    } else {
                        db.collection("users").document(currentUid)
                                .collection("friends").document(friendId)
                                .set(new Friend(friendId, "pending"));
                    }
                });
    }

    private void shareAnimals(DocumentSnapshot user) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String friendId = user.getId();

        db.collection("users").document(friendId)
                .collection("sharedAnimalsRequests")
                .document(currentUserId)
                .set(new SharedAnimalsRequest(currentUserId, "pending"))
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Share request sent!", Toast.LENGTH_SHORT).show();
                });
    }
}