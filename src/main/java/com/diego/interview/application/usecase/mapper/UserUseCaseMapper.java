package com.diego.interview.application.usecase.mapper;

import com.diego.interview.application.usecase.dto.CreateUserCommand;
import com.diego.interview.application.usecase.dto.UpdateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.domain.model.Phone;
import com.diego.interview.domain.model.User;

import java.util.List;

public final class UserUseCaseMapper {

    private UserUseCaseMapper() {
        // utility class
    }


    public static UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse resp = new UserResponse();
        resp.setId(user.getId() != null ? user.getId().toString() : null);
        resp.setCreated(user.getCreatedAt());
        resp.setModified(user.getUpdatedAt());
        resp.setLastLogin(user.getLastLogin());
        resp.setToken(user.getToken());
        resp.setActive(user.isActive());
        resp.setName(user.getName());
        resp.setEmail(user.getEmail());

        if (user.getPhones() != null) {
            resp.setPhones(
                    user.getPhones().stream()
                            .map(UserUseCaseMapper::toPhoneResponse)
                            .toList()
            );
        }

        return resp;
    }

    public static UserResponse.PhoneResponse toPhoneResponse(Phone phone) {
        if (phone == null) {
            return null;
        }

        UserResponse.PhoneResponse pr = new UserResponse.PhoneResponse();
        pr.setNumber(phone.getNumber());
        pr.setCityCode(phone.getCityCode());
        pr.setCountryCode(phone.getCountryCode());
        return pr;
    }


    public static List<Phone> toDomainPhonesFromCreate(List<CreateUserCommand.PhoneCommand> phoneCommands) {
        if (phoneCommands == null) {
            return List.of();
        }

        return phoneCommands.stream()
                .map(p -> Phone.builder()
                        .number(p.getNumber())
                        .cityCode(p.getCityCode())
                        .countryCode(p.getCountryCode())
                        .build())
                .toList();
    }


    public static List<Phone> toDomainPhonesFromUpdate(List<UpdateUserCommand.PhoneCommand> phoneCommands) {
        if (phoneCommands == null) {
            return List.of();
        }

        return phoneCommands.stream()
                .map(p -> Phone.builder()
                        .number(p.getNumber())
                        .cityCode(p.getCityCode())
                        .countryCode(p.getCountryCode())
                        .build())
                .toList();
    }
}
