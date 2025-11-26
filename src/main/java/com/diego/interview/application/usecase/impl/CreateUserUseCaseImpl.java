package com.diego.interview.application.usecase.impl;

import com.diego.interview.domain.exception.BusinessException;
import com.diego.interview.domain.model.Phone;
import com.diego.interview.domain.model.User;
import com.diego.interview.domain.port.TokenProviderPort;
import com.diego.interview.domain.port.UserRepositoryPort;
import com.diego.interview.application.usecase.CreateUserUseCase;
import com.diego.interview.application.usecase.dto.CreateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateUserUseCaseImpl.class);

    private final UserRepositoryPort userRepositoryPort;
    private final Pattern emailPattern;
    private final Pattern passwordPattern;
    private final TokenProviderPort tokenProviderPort;

    public CreateUserUseCaseImpl(UserRepositoryPort userRepositoryPort,
                                 Pattern emailPattern,
                                 Pattern passwordPattern,
                                 TokenProviderPort tokenProviderPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.emailPattern = emailPattern;
        this.passwordPattern = passwordPattern;
        this.tokenProviderPort = tokenProviderPort;
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

            String encodedPassword = rawPassword;

            User userToSave = buildUser(command, email, encodedPassword, now);

            User saved = userRepositoryPort.save(userToSave);

            long elapsedNanos = System.nanoTime() - start;
            log.info("User created successfully. id={}, email={}, elapsedMs={}",
                    saved.getId(), saved.getEmail(), elapsedNanos / 1_000_000);

            return toUserResponse(saved);

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

        List<Phone> phones = toPhones(command.getPhones());

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

    private List<Phone> toPhones(List<CreateUserCommand.PhoneCommand> phoneCommands) {
        if (phoneCommands == null) {
            log.debug("No phones provided in request.");
            return List.of();
        }

        return phoneCommands.stream()
                .map(p -> Phone.builder()
                        .number(p.getNumber())
                        .cityCode(p.getCityCode())
                        .countryCode(p.getCountryCode())
                        .build())
                .collect(Collectors.toList());
    }

    private UserResponse toUserResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId().toString());
        resp.setCreated(user.getCreatedAt());
        resp.setModified(user.getUpdatedAt());
        resp.setLastLogin(user.getLastLogin());
        resp.setToken(user.getToken());
        resp.setActive(user.isActive());
        resp.setName(user.getName());
        resp.setEmail(user.getEmail());
        resp.setPhones(
                user.getPhones().stream()
                        .map(this::toPhoneResponse)
                        .collect(Collectors.toList())
        );
        return resp;
    }

    private UserResponse.PhoneResponse toPhoneResponse(Phone phone) {
        UserResponse.PhoneResponse phoneResponse = new UserResponse.PhoneResponse();
        phoneResponse.setCityCode(phone.getCityCode());
        phoneResponse.setNumber(phone.getNumber());
        phoneResponse.setCountryCode(phone.getCountryCode());
        return phoneResponse;
    }
}
