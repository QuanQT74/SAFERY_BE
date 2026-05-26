package com.example.banckend.auth.service;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.banckend.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        com.example.banckend.auth.entity.User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        return User.builder()
                .username(user.getId().toString())
                .password(user.getPasswordHash())
                .authorities(Collections.emptyList())
                .build();
    }
}