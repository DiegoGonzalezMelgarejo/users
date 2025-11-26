package com.diego.interview.infraestructure.in.rest;

import com.diego.interview.application.usecase.CreateUserUseCase;
import com.diego.interview.application.usecase.dto.CreateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.infraestructure.in.rest.dto.CreateUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody @Valid CreateUserRequest request) {
        CreateUserCommand command = mapToCommand(request);
        UserResponse response = createUserUseCase.createUser(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private CreateUserCommand mapToCommand(CreateUserRequest request) {
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

