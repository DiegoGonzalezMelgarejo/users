package com.diego.interview.application.usecase.impl;

import com.diego.interview.application.usecase.GetUserByIdUseCase;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.application.usecase.mapper.UserUseCaseMapper;
import com.diego.interview.domain.exception.BusinessException;
import com.diego.interview.domain.model.User;
import com.diego.interview.domain.port.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class GetUserByIdUseCaseImpl implements GetUserByIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetUserByIdUseCaseImpl.class);

    private final UserRepositoryPort userRepository;

    public GetUserByIdUseCaseImpl(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse getById(UUID id) {
        log.info("Fetching user by id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found. id={}", id);
                    return new BusinessException("user.notFound", id.toString());
                });

        log.info("User found. id={}", id);

        return UserUseCaseMapper.toUserResponse(user);
    }
}