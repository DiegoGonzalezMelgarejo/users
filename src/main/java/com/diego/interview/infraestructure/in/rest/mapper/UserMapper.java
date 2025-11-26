package com.diego.interview.infraestructure.in.rest.mapper;

import com.diego.interview.application.usecase.dto.CreateUserCommand;
import com.diego.interview.application.usecase.dto.UpdateUserCommand;
import com.diego.interview.infraestructure.in.rest.dto.CreateUserRequest;
import com.diego.interview.infraestructure.in.rest.dto.UpdateUserRequest;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    private UserMapper(){
    }
    public static UpdateUserCommand mapToUpdateCommand(UpdateUserRequest request) {

        List<UpdateUserCommand.PhoneCommand> phones = null;

        if (request.getPhones() != null) {
            phones = request.getPhones().stream()
                    .map(p -> UpdateUserCommand.PhoneCommand.builder()
                            .number(p.getNumero())
                            .cityCode(p.getCodigoCiudad())
                            .countryCode(p.getCodigoPais())
                            .build())
                    .toList();
        }

        return UpdateUserCommand.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .active(request.getActive())
                .phones(phones)
                .build();
    }
    public static CreateUserCommand mapToCommand(CreateUserRequest request) {
        List<CreateUserCommand.PhoneCommand> phoneCommands =
                request.getPhones() == null ? List.of() :
                        request.getPhones().stream()
                                .map(p -> CreateUserCommand.PhoneCommand.builder()
                                        .number(p.getNumero())
                                        .cityCode(p.getCodigoCiudad())
                                        .countryCode(p.getCodigoPais())
                                        .build()
                                )
                                .collect(Collectors.toList());

        return CreateUserCommand.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phones(phoneCommands)
                .build();
    }
}
