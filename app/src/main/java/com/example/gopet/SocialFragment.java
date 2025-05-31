package com.example.gopet;

import android.content.Intent;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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

        view.setVisibility(View.VISIBLE);
        view.bringToFront();
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
            public void onUsernameClick(DocumentSnapshot user) {
                openUserProfile(user);
            }
            @Override
            public void onShareAnimalsClicked(DocumentSnapshot user) {
                shareAnimals(user);
            }
            @Override
            public void onProfileImageClicked(DocumentSnapshot user) {
                openUserProfile(user);
            }
        });
        userAdapter.setOnRemoveFriendClickListener(user -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String friendId = user.getId();

            db.collection("users").document(currentUserId).collection("friends").document(friendId)
                    .delete();

            db.collection("users").document(friendId).collection("friends").document(currentUserId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Friend removed", Toast.LENGTH_SHORT).show();
                        loadAllUsers(); // sau showFriends(); dacă e prieteni
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error removing friend", Toast.LENGTH_SHORT).show();
                    });
        });
        userAdapter.setOnReloadAnimalsListener(() -> {
            loadFriends();
            showFriends();
        });
        recyclerView.setAdapter(userAdapter);
        loadFriends();

        btnAllUsers.setOnClickListener(v -> loadAllUsers());
        btnFriends.setOnClickListener(v -> showFriends());

        Bundle args = getArguments();
        if (args != null && "friends".equals(args.getString("defaultTab"))) {
            btnFriends.post(() -> {
                btnFriends.performClick();
            });
        } else {
            loadAllUsers();
        }


        return view;
    }

    private void loadAllUsers() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").get().addOnSuccessListener(querySnapshot -> {
            List<DocumentSnapshot> tempList = new ArrayList<>();
            List<DocumentSnapshot> allUsers = querySnapshot.getDocuments();

            int[] remaining = {allUsers.size()};

            for (DocumentSnapshot userDoc : allUsers) {
                String userId = userDoc.getId();
                if (userId.equals(currentUserId)) {
                    remaining[0]--;
                    continue;
                }

                boolean isFriend = false;
                for (DocumentSnapshot friend : friendsList) {
                    if (friend.getId().equals(userId)) {
                        isFriend = true;
                        break;
                    }
                }

                if (!isFriend) {
                    db.collection("users").document(userId)
                            .collection("friends").document(currentUserId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (doc == null || !doc.exists()) {
                                    tempList.add(userDoc);
                                }
                                remaining[0]--;
                                if (remaining[0] == 0) {
                                    updateUsersListSafely(tempList);
                                }
                            });
                } else {
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        updateUsersListSafely(tempList);
                    }
                }
            }
        });
    }
    private void openUserProfile(DocumentSnapshot user) {
        String userId = user.getId();
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
    private void updateUsersListSafely(List<DocumentSnapshot> updatedList) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            usersList.clear();
            usersList.addAll(updatedList);
            userAdapter.notifyDataSetChanged();
        });
    }


    private void showFriends() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Set<String> addedUserIds = new HashSet<>();
        List<DocumentSnapshot> tempList = new ArrayList<>();

        db.collection("users").get().addOnSuccessListener(allUsersSnapshot -> {
            List<DocumentSnapshot> allUsers = allUsersSnapshot.getDocuments();
            int[] pendingCalls = {0}; // numărăm câte requests asincrone sunt

            for (DocumentSnapshot userDoc : allUsers) {
                String userId = userDoc.getId();
                if (userId.equals(currentUserId)) continue;

                pendingCalls[0] += 2;

                db.collection("users").document(currentUserId).collection("friends")
                        .document(userId)
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists() && addedUserIds.add(userId)) {
                                tempList.add(userDoc);
                            }
                            if (--pendingCalls[0] == 0) updateAdapterWithList(tempList);
                        });

                db.collection("users").document(userId).collection("friends")
                        .document(currentUserId)
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists() && addedUserIds.add(userId)) {
                                tempList.add(userDoc);
                            }
                            if (--pendingCalls[0] == 0) updateAdapterWithList(tempList);
                        });
            }

            if (pendingCalls[0] == 0) updateAdapterWithList(tempList);
        });
    }

    private void updateAdapterWithList(List<DocumentSnapshot> list) {
        usersList.clear();
        usersList.addAll(list);
        userAdapter.notifyDataSetChanged();
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
                                .update("status", "confirmed")
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(), "Friend request confirmed!", Toast.LENGTH_SHORT).show();
                                    refreshSocialData();
                                });
                    } else {
                        db.collection("users").document(currentUid)
                                .collection("friends").document(friendId)
                                .set(new Friend(friendId, "pending"))
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(), "Friend request sent!", Toast.LENGTH_SHORT).show();
                                    refreshSocialData();
                                });
                    }
                });
    }


    private void refreshSocialData() {
        loadFriends();
        loadAllUsers();
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