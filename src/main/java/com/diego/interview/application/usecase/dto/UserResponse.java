package com.diego.interview.application.usecase.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserResponse {
    private String id;
    private LocalDateTime created;
    private LocalDateTime modified;
    private LocalDateTime lastLogin;
    private String token;
    private boolean active;
    private String name;
    private String email;
    private List<PhoneResponse> phones;

    @Getter
    @Setter
    public static class PhoneResponse {
        private String number;
        private String cityCode;
        private String countryCode;

    }

}