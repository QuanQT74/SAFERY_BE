package com.example.banckend.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.banckend.auth.entity.RefreshToken;
import com.example.banckend.auth.repository.RefreshTokenRepository;
import com.example.banckend.conmon.exception.BadRequestException;
import com.example.banckend.auth.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtService.generateRefreshToken(user));
        refreshToken.setRevoked(false);
        refreshToken.setExpiredAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000));
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiredAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new BadRequestException("Refresh token expired");
        }
        if (token.getRevoked()) {
            throw new BadRequestException("Refresh token revoked");
        }
        return token;
    }

    @Transactional
    public String generateNewAccessToken(RefreshToken oldToken) {
        User user = refreshTokenRepository.findById(oldToken.getId())
                .map(RefreshToken::getUser)
                .orElseThrow(() -> new BadRequestException("Refresh token not found"));
        return jwtService.generateAccessToken(user);
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Refresh token is invalid"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeAllRefreshTokensOfUser(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserAndRevokedFalse(user);

        for (RefreshToken token : tokens) {
            token.setRevoked(true);
        }

        refreshTokenRepository.saveAll(tokens);
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

}
