package com.diego.interview.infraestructure.in.rest;

import com.diego.interview.application.usecase.CreateUserUseCase;
import com.diego.interview.application.usecase.DeleteUserUseCase;
import com.diego.interview.application.usecase.GetUserByIdUseCase;
import com.diego.interview.application.usecase.ListUsersUseCase;
import com.diego.interview.application.usecase.LoginUserUseCase;
import com.diego.interview.application.usecase.UpdateUserUseCase;
import com.diego.interview.application.usecase.dto.CreateUserCommand;
import com.diego.interview.application.usecase.dto.PagedResponse;
import com.diego.interview.application.usecase.dto.UpdateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.infraestructure.in.rest.dto.CreateUserRequest;
import com.diego.interview.infraestructure.in.rest.dto.LoginRequest;
import com.diego.interview.infraestructure.in.rest.dto.UpdateUserRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.diego.interview.infraestructure.in.rest.mapper.UserMapper.mapToCommand;
import static com.diego.interview.infraestructure.in.rest.mapper.UserMapper.mapToUpdateCommand;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    public UserController(CreateUserUseCase createUserUseCase, LoginUserUseCase loginUserUseCase,
                          ListUsersUseCase listUsersUseCase, GetUserByIdUseCase getUserByIdUseCase,
                          DeleteUserUseCase deleteUserUseCase, UpdateUserUseCase updateUserUseCase
                          ) {
        this.createUserUseCase = createUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.updateUserUseCase= updateUserUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody @Valid CreateUserRequest request) {
        CreateUserCommand command = mapToCommand(request);
        UserResponse response = createUserUseCase.createUser(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody @Valid LoginRequest request) {
        UserResponse response = loginUserUseCase.login(
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<UserResponse> response = listUsersUseCase.listUsers(page, size);
        return ResponseEntity.ok(response);
    }



    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        UserResponse response = getUserByIdUseCase.getById(id);
        return ResponseEntity.ok(response);
    }
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteUserUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUserRequest request
    ) {
        UpdateUserCommand command = mapToUpdateCommand(request);
        UserResponse response = updateUserUseCase.update(id, command);
        return ResponseEntity.ok(response);
    }

}

