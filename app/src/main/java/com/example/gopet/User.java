package com.example.gopet;

public class User {
    private String id;
    private String username;
    private String email;
    private String dob;

    public User() {}

    public User(String id, String username, String dob, String email) {
        this.id = id;
        this.username = username;
        this.dob = dob;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDob() {
        return dob;
    }

    public String getEmail() {
        return email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
