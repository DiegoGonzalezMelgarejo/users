package com.diego.interview.application.usecase.impl;
import com.diego.interview.application.usecase.ListUsersUseCase;
import com.diego.interview.application.usecase.dto.PagedResponse;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.application.usecase.mapper.UserUseCaseMapper;
import com.diego.interview.domain.model.User;
import com.diego.interview.domain.port.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ListUsersUseCaseImpl implements ListUsersUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListUsersUseCaseImpl.class);

    private final UserRepositoryPort userRepository;

    public ListUsersUseCaseImpl(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public PagedResponse<UserResponse> listUsers(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;

        log.info("Listing users. page={}, size={}", safePage, safeSize);

        List<User> users = userRepository.findAllPaged(safePage, safeSize);
        long total = userRepository.countAll();

        List<UserResponse> content = users.stream()
                .map(UserUseCaseMapper::toUserResponse)
                .toList();

        PagedResponse<UserResponse> resp = new PagedResponse<>();
        resp.setContent(content);
        resp.setPage(safePage);
        resp.setSize(safeSize);
        resp.setTotalElements(total);
        int totalPages = (int) Math.ceil((double) total / (double) safeSize);
        resp.setTotalPages(totalPages);

        return resp;
    }
}