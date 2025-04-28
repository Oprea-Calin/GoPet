package com.example.gopet;

public class Friend {
    private String friendId, status;

    public Friend() {
    }

    public Friend(String friendId, String status) {
        this.friendId = friendId;
        this.status = status;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
