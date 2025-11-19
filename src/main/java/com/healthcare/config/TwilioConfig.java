package com.healthcare.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class TwilioConfig {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.api.key:}")
    private String apiKey;

    @Value("${twilio.api.secret:}")
    private String apiSecret;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @PostConstruct
    public void init() {
        if (accountSid.isEmpty() || authToken.isEmpty()) {
            log.warn("Twilio credentials not configured. Video consultation features will not be available.");
            return;
        }

        try {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Twilio", e);
        }
    }

    public String getAccountSid() {
        return accountSid;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }
}
