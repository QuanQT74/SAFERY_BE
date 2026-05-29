package com.example.banckend.auth.service;

import com.example.banckend.auth.entity.FcmToken;
import com.example.banckend.auth.entity.User;
import com.example.banckend.auth.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public void registerToken(User user, String token, String deviceId) {
        // Kiểm tra token đã tồn tại chưa
        fcmTokenRepository.findByToken(token).ifPresentOrElse(
            existingToken -> {
                // Token đã tồn tại, cập nhật thời gian
                existingToken.setRegisteredAt(LocalDateTime.now());
                if (deviceId != null) {
                    existingToken.setDeviceId(deviceId);
                }
                fcmTokenRepository.save(existingToken);
                log.info("[FCM] Updated existing token for userId={}, deviceId={}", user.getId(), deviceId);
            },
            () -> {
                // Token mới, tạo mới
                FcmToken fcmToken = FcmToken.builder()
                        .user(user)
                        .token(token)
                        .deviceId(deviceId)
                        .registeredAt(LocalDateTime.now())
                        .build();
                fcmTokenRepository.save(fcmToken);
                log.info("[FCM] Registered new token for userId={}, deviceId={}", user.getId(), deviceId);
            }
        );
    }

    @Transactional
    public void removeToken(User user, String token) {
        fcmTokenRepository.deleteByUserAndToken(user, token);
        log.info("[FCM] Removed token for userId={}", user.getId());
    }

    @Transactional(readOnly = true)
    public List<FcmToken> getAllTokensForUser(User user) {
        return fcmTokenRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<String> getAllTokenStringsForUser(User user) {
        return fcmTokenRepository.findByUser(user).stream()
                .map(FcmToken::getToken)
                .toList();
    }

    @Transactional
    public void removeAllTokensForUser(Long userId) {
        // Delete all tokens for this user
        List<FcmToken> tokens = fcmTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(userId))
                .toList();
        fcmTokenRepository.deleteAll(tokens);
        log.info("[FCM] Removed all tokens for userId={}", userId);
    }
}
