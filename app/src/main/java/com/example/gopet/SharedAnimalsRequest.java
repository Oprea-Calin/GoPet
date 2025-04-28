package com.example.gopet;

public class SharedAnimalsRequest {
    private String senderId;
    private String status;
    public SharedAnimalsRequest() {}

    public SharedAnimalsRequest(String senderId, String status) {
        this.senderId = senderId;
        this.status = status;
    }

    public String getSenderId() { return senderId; }
    public String getStatus() { return status; }
}
