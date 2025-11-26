package com.diego.interview.application.usecase.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UpdateUserCommand {

    private final String name;
    private final String email;
    private final String password;
    private final Boolean active;
    private final List<PhoneCommand> phones;

    @Getter
    @Builder
    public static class PhoneCommand {
        private final String number;
        private final String cityCode;
        private final String countryCode;
    }
}
