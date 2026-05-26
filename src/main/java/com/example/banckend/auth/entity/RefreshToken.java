package com.example.banckend.auth.entity;
import java.time.LocalDateTime;
import com.example.banckend.conmon.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken  extends BaseEntity {
    
   @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Boolean revoked = false;

    @Column(nullable = false)
    private LocalDateTime expiredAt;
}
