package com.example.banckend.auth.service;

import org.springframework.stereotype.Service;

import com.example.banckend.auth.entity.User;

@Service
public class JwtService {
     public String generateAccessToken(User user) {
        return "access-token-for-user-" + user.getId();
    }

    public String generateRefreshToken(User user) {
        return "refresh-token-for-user-" + user.getId();
    }
}
