package com.diego.interview.application.usecase.impl;

import com.diego.interview.application.usecase.DeleteUserUseCase;
import com.diego.interview.domain.exception.BusinessException;
import com.diego.interview.domain.port.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DeleteUserUseCaseImpl implements DeleteUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteUserUseCaseImpl.class);

    private final UserRepositoryPort userRepository;

    public DeleteUserUseCaseImpl(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void deleteById(UUID id) {
        log.info("Attempting to delete user. id={}", id);

        boolean exists = userRepository.findById(id).isPresent();

        if (!exists) {
            log.warn("Delete user failed: user not found. id={}", id);
            throw new BusinessException("user.notFound", id.toString());
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully. id={}", id);
    }
}
