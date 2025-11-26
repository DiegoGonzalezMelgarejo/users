package com.diego.interview.application.usecase;

import com.diego.interview.application.usecase.dto.PagedResponse;
import com.diego.interview.application.usecase.dto.UserResponse;

public interface ListUsersUseCase {

    PagedResponse<UserResponse> listUsers(int page, int size);
}
