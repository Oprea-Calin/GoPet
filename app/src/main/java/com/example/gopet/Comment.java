package com.example.gopet;

public class Comment {
    public String authorID, authorUsername, authorImageBase64, comment, timestamp;
    public Comment() {}

    public Comment(String authorId, String authorUsername, String authorImageBase64, String comment, String timestamp) {
        this.authorID = authorId;
        this.authorUsername = authorUsername;
        this.authorImageBase64 = authorImageBase64;
        this.comment = comment;
        this.timestamp = timestamp;
    }
}
