package com.example.banckend.auth.service;

import com.google.api.client.util.Value;

import jakarta.annotation.PostConstruct;

public class SmsService {
    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        com.twilio.Twilio.init(accountSid, authToken);
    }
    public void sendSms(String to, String message) {
        com.twilio.rest.api.v2010.account.Message.creator(
                new com.twilio.type.PhoneNumber(to),
                new com.twilio.type.PhoneNumber(fromNumber),
                message
        ).create();
    }
}
