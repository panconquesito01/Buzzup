package com.MagicDevelopers.buzzup.Modelos;

public class UserSuggestion {
    private String id;
    private String fullName;
    private String username;
    private String profileUrl;
    private boolean isFollowing;

    public UserSuggestion(String id, String fullName, String username, String profileUrl, boolean isFollowing) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.profileUrl = profileUrl;
        this.isFollowing = isFollowing;
    }

    public String getUserId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }
}
