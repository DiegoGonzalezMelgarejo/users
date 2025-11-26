package com.diego.interview.application.usecase;

import com.diego.interview.application.usecase.dto.UserResponse;

public interface LoginUserUseCase {
    UserResponse login(String email, String password);
}
