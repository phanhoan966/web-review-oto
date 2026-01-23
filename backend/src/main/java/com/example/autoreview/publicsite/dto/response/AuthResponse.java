package com.example.autoreview.publicsite.dto.response;

import com.example.autoreview.publicsite.dto.response.UserProfileDto;

public class AuthResponse {

    private UserProfileDto user;

    public AuthResponse() {
    }

    public AuthResponse(UserProfileDto user) {
        this.user = user;
    }

    public UserProfileDto getUser() {
        return user;
    }

    public void setUser(UserProfileDto user) {
        this.user = user;
    }
}
