package com.example.banckend.profile.controller;

import com.example.banckend.auth.service.JwtService;
import com.example.banckend.conmon.exception.BadRequestException;
import com.example.banckend.conmon.response.ApiResponse;
import com.example.banckend.profile.dto.request.UpdateProfileRequest;
import com.example.banckend.profile.dto.response.ProfileResponse;
import com.example.banckend.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "APIs for user profile")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Missing or invalid Authorization header. Use: Authorization: Bearer <accessToken>");
        }

        String accessToken = authorizationHeader.substring(7);
        ProfileResponse response = profileService.getMyProfileByAccessToken(accessToken);

        return ResponseEntity.ok(
                ApiResponse.<ProfileResponse>builder()
                        .message("Success")
                        .data(response)
                        .build()
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Missing or invalid Authorization header. Use: Authorization: Bearer <accessToken>");
        }

        String accessToken = authorizationHeader.substring(7);
        ProfileResponse response = profileService.updateMyProfile(accessToken, request);

        return ResponseEntity.ok(
                ApiResponse.<ProfileResponse>builder()
                        .message("Profile updated successfully")
                        .data(response)
                        .build()
        );
    }
}