package com.example.banckend.auth.repository;

import com.example.banckend.auth.entity.FcmToken;
import com.example.banckend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    List<FcmToken> findByUser(User user);

    Optional<FcmToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUserAndToken(User user, String token);
}
