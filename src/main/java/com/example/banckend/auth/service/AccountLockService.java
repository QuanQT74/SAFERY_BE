package com.example.banckend.auth.service;

import com.example.banckend.auth.entity.User;
import com.example.banckend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountLockService {

    private final UserRepository userRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockAccount(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            userRepository.save(user);
            log.warn("Account locked for userId: {} due to too many failed login attempts", userId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementFailedAttempts(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            int failedAttempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
            user.setFailedLoginAttempts(failedAttempts);
            userRepository.save(user);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

    public boolean isAccountLocked(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        return user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil());
    }

    public boolean isMaxAttemptsReached(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        int failedAttempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts());
        return failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    public int getFailedAttempts(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return 0;
        return (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts());
    }
}
