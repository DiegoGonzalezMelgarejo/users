package com.diego.interview.application.usecase;

import com.diego.interview.application.usecase.dto.UpdateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;

import java.util.UUID;

public interface UpdateUserUseCase {
    UserResponse update(UUID id, UpdateUserCommand command);
}
