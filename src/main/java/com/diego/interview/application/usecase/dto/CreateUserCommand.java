package com.diego.interview.application.usecase.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreateUserCommand {
    private String name;
    private String email;
    private String password;
    private List<PhoneCommand> phones;


    @Getter
    @Builder
    public static class PhoneCommand {
        private String number;
        private String cityCode;
        private String countryCode;
    }
}
