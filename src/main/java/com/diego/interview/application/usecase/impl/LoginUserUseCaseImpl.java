package com.diego.interview.application.usecase.impl;

import com.diego.interview.application.usecase.LoginUserUseCase;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.application.usecase.mapper.UserUseCaseMapper;
import com.diego.interview.domain.exception.BusinessException;
import com.diego.interview.domain.model.User;
import com.diego.interview.domain.port.PasswordEncoderPort;
import com.diego.interview.domain.port.TokenProviderPort;
import com.diego.interview.domain.port.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class LoginUserUseCaseImpl implements LoginUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUserUseCaseImpl.class);

    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final PasswordEncoderPort passwordEncoder;

    public LoginUserUseCaseImpl(UserRepositoryPort userRepository,
                                TokenProviderPort tokenProvider,
                                PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse login(String email, String rawPassword) {
        log.info("Starting login process. email={}", email);
        LocalDateTime now = LocalDateTime.now();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found. email={}", email);
                    return new BusinessException("user.login.invalidCredentials", email);
                });

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("Login failed: invalid password. email={}", email);
            throw new BusinessException("user.login.invalidCredentials", email);
        }

        user.setLastLogin(now);
        String token = tokenProvider.generateToken(user);
        user.setToken(token);

        User saved = userRepository.save(user);
        log.info("Login successful. email={}", email);

        return UserUseCaseMapper.toUserResponse(saved);
    }
}