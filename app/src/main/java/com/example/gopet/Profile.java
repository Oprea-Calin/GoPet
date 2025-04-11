package com.example.gopet;

public class Profile {
    private String username;
    private String quote;
    private String base64Image;

    public Profile(String username, String quote, String base64Image) {
        this.username = username;
        this.quote = quote;
        this.base64Image = base64Image;
    }

    public String getUsername() {
        return username;
    }

    public String getQuote() {
        return quote;
    }

    public String getBase64Image() {
        return base64Image;
    }
}
