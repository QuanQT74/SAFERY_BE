package com.example.banckend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.banckend.auth.entity.OtpVerification;
import com.example.banckend.conmon.enums.OtpPurpose;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    boolean existsByPhoneNumberAndVerifiedFalse(String phoneNumber);
    Optional<OtpVerification> findTopByPhoneNumberAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
            String phoneNumber, 
            OtpPurpose purpose);
}
