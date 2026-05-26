package com.example.banckend.device.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PairDeviceResponse {

    private String deviceCode;
    private String deviceSecret;
    private String deviceName;
    private String nickname;
}