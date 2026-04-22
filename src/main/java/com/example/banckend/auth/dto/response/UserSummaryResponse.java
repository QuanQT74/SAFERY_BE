package com.example.banckend.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserSummaryResponse {
    
    private Long id;

    private String fullName;

    private String phoneNumber;

    private Boolean phoneVerified;
}
