package com.journalapp.john.journalapp.Models;

@SuppressWarnings("unused")
public class User {
    private String username;
    private String imageUrl;

    public User(String username, String imageUrl) {
        this.username = username;
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
