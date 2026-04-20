package  com.example.banckend.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

import com.example.banckend.auth.entity.User;
import com.example.banckend.conmon.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "notification_settings")
public class NotificationSettings extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Boolean pushEnabled = true;

    @Column(nullable = false)
    private Boolean soundEnabled = true;

    @Column(nullable = false)
    private Boolean dangerAlertsEnabled = true;

    @Column(nullable = false)
    private Boolean warningAlertsEnabled = true;

    @Column(nullable = false)
    private Boolean offlineAlertsEnabled = true;

    @Column(nullable = false)
    private Boolean fanStatusAlertsEnabled = false;

    private LocalTime quietHoursStart;

    private LocalTime quietHoursEnd;
}