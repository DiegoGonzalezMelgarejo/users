package com.diego.interview.application.usecase;

import com.diego.interview.application.usecase.dto.UserResponse;

import java.util.UUID;

public interface GetUserByIdUseCase {
    UserResponse getById(UUID id);
}
