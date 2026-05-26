package com.example.banckend.auth.mapper;

import com.example.banckend.auth.dto.request.RegisterRequest;
import com.example.banckend.auth.dto.response.RegisterResponse;
import com.example.banckend.auth.dto.response.UserSummaryResponse;
import com.example.banckend.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(request.getPassword());
        return user;
    }

    public RegisterResponse toResponse(User user) {
        return RegisterResponse.builder()
                .message("Registration successful")
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .phoneVerified(user.getPhoneVerified())
                .build();
    }

    public UserSummaryResponse toSummaryResponse(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .phoneVerified(user.getPhoneVerified())
                .build();
    }
}
