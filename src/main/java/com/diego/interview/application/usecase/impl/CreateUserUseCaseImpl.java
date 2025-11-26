package com.diego.interview.application.usecase.impl;
import com.diego.interview.application.usecase.CreateUserUseCase;
import com.diego.interview.application.usecase.dto.CreateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.application.usecase.mapper.UserUseCaseMapper;
import com.diego.interview.domain.exception.BusinessException;
import com.diego.interview.domain.model.Phone;
import com.diego.interview.domain.model.User;
import com.diego.interview.domain.port.PasswordEncoderPort;
import com.diego.interview.domain.port.TokenProviderPort;
import com.diego.interview.domain.port.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateUserUseCaseImpl.class);

    private final UserRepositoryPort userRepositoryPort;
    private final Pattern emailPattern;
    private final Pattern passwordPattern;
    private final TokenProviderPort tokenProviderPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public CreateUserUseCaseImpl(UserRepositoryPort userRepositoryPort,
                                 Pattern emailPattern,
                                 Pattern passwordPattern,
                                 TokenProviderPort tokenProviderPort,
                                 PasswordEncoderPort passwordEncoderPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.emailPattern = emailPattern;
        this.passwordPattern = passwordPattern;
        this.tokenProviderPort = tokenProviderPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public UserResponse createUser(CreateUserCommand command) {
        LocalDateTime now = LocalDateTime.now();
        long start = System.nanoTime();

        log.info("Starting user creation. email={}, name={}", command.getEmail(), command.getName());

        try {
            String email = validateEmailFormat(command.getEmail());
            String rawPassword = validatePasswordFormat(command.getPassword());
            ensureEmailNotExists(email);

            String encodedPassword = passwordEncoderPort.encode(rawPassword);

            User userToSave = buildUser(command, email, encodedPassword, now);

            User saved = userRepositoryPort.save(userToSave);

            long elapsedNanos = System.nanoTime() - start;
            log.info("User created successfully. id={}, email={}, elapsedMs={}",
                    saved.getId(), saved.getEmail(), elapsedNanos / 1_000_000);

            return UserUseCaseMapper.toUserResponse(saved);

        } catch (BusinessException ex) {
            long elapsedNanos = System.nanoTime() - start;
            log.warn("Business error while creating user. email={}, code={}, elapsedMs={}",
                    command.getEmail(), ex.getCode(), elapsedNanos / 1_000_000);
            throw ex;
        } catch (Exception ex) {
            long elapsedNanos = System.nanoTime() - start;
            log.error("Unexpected error while creating user. email={}, elapsedMs={}",
                    command.getEmail(), elapsedNanos / 1_000_000, ex);
            throw ex;
        }
    }

    private String validateEmailFormat(String email) {
        if (!emailPattern.matcher(email).matches()) {
            log.debug("Invalid email format detected. email={}", email);
            throw new BusinessException("user.email.invalid", email);
        }
        return email;
    }

    private String validatePasswordFormat(String password) {
        if (!passwordPattern.matcher(password).matches()) {
            log.debug("Password format validation failed.");
            throw new BusinessException("user.password.invalid");
        }
        return password;
    }

    private void ensureEmailNotExists(String email) {
        userRepositoryPort.findByEmail(email)
                .ifPresent(u -> {
                    log.debug("Email already exists in database. email={}", email);
                    throw new BusinessException("user.email.exists", email);
                });
    }

    private User buildUser(CreateUserCommand command,
                           String email,
                           String encodedPassword,
                           LocalDateTime now) {

        List<Phone> phones = UserUseCaseMapper.toDomainPhonesFromCreate(command.getPhones());

        User tmpUser = User.builder()
                .name(command.getName())
                .email(email)
                .password(encodedPassword)
                .phones(phones)
                .createdAt(now)
                .updatedAt(now)
                .lastLogin(now)
                .active(true)
                .build();

        String token = tokenProviderPort.generateToken(tmpUser);
        tmpUser.setToken(token);

        log.debug("User entity built before persist. email={}, phonesCount={}",
                email,
                phones != null ? phones.size() : 0);

        return tmpUser;
    }
}