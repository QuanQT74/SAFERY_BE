package com.example.banckend.auth.entity;

import com.example.banckend.conmon.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "fcm_tokens")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(length = 100)
    private String deviceId;

    @Column(nullable = false)
    private LocalDateTime registeredAt;
}
