package com.diego.interview.application.usecase;

import com.diego.interview.application.usecase.dto.CreateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;

public interface CreateUserUseCase {
    UserResponse createUser(CreateUserCommand command);
}
