package com.example.gopet;

public class PetSittingRequest {
    public String id;
    public String postId;
    public String userId;
    public String message;
    public String status = "pending";

    public PetSittingRequest() {}

    public PetSittingRequest(String id, String postId, String userId, String message) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.message = message;
    }
}