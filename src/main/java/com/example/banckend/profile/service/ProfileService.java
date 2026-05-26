package com.example.banckend.profile.service;

import com.example.banckend.auth.entity.User;
import com.example.banckend.auth.repository.UserRepository;
import com.example.banckend.auth.service.JwtService;
import com.example.banckend.conmon.exception.BadRequestException;
import com.example.banckend.profile.dto.request.UpdateProfileRequest;
import com.example.banckend.profile.dto.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public ProfileResponse updateMyProfile(String accessToken, UpdateProfileRequest request) {
        log.info("Processing updateMyProfile");

        if (jwtService.isTokenExpired(accessToken)) {
            throw new BadRequestException("Access token has expired");
        }

        String userId = jwtService.extractUserId(accessToken);
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setFullName(request.getFullName());
        userRepository.save(user);

        return ProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .phoneVerified(user.getPhoneVerified())
                .build();
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfileByAccessToken(String accessToken) {
        log.info("Processing getMyProfileByAccessToken");

        if (jwtService.isTokenExpired(accessToken)) {
            log.warn("Token expired");
            throw new BadRequestException("Access token has expired");
        }

        String userId = jwtService.extractUserId(accessToken);
        log.info("UserId from token: {}", userId);

        Long userIdLong;
        try {
            userIdLong = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            log.error("Invalid userId format: {}", userId);
            throw new BadRequestException("Invalid token: userId not valid");
        }

        User user = userRepository.findById(userIdLong)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userIdLong);
                    return new BadRequestException("User not found");
                });

        log.info("Found user: {}", user.getPhoneNumber());

        return ProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .phoneVerified(user.getPhoneVerified())
                .build();
    }
}