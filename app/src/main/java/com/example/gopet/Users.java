package com.example.gopet;

public class Users {
    private int id;
    private String username, password;
    private String dob;

    public void setDob(String dob) {
        this.dob = dob;
    }

    public Users(int id, String username, String dob) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.dob = dob;
    }

    public String getDob() {
        return dob;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public int getId() {
        return id;
    }
}
