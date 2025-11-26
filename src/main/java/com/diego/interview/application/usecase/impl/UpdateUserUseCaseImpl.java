package com.diego.interview.application.usecase.impl;

import com.diego.interview.application.usecase.UpdateUserUseCase;
import com.diego.interview.application.usecase.dto.UpdateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.domain.exception.BusinessException;
import com.diego.interview.domain.model.Phone;
import com.diego.interview.domain.model.User;
import com.diego.interview.domain.port.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class UpdateUserUseCaseImpl implements UpdateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserUseCaseImpl.class);

    private final UserRepositoryPort userRepository;
    private final Pattern emailPattern;
    private final Pattern passwordPattern;

    public UpdateUserUseCaseImpl(UserRepositoryPort userRepository,
                                 Pattern emailPattern,
                                 Pattern passwordPattern) {
        this.userRepository = userRepository;
        this.emailPattern = emailPattern;
        this.passwordPattern = passwordPattern;
    }

    @Override
    public UserResponse update(UUID id, UpdateUserCommand command) {
        log.info("Updating user partially. id={}", id);
        LocalDateTime now = LocalDateTime.now();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("user.notFound", id.toString()));

        if (command.getName() != null && !command.getName().isBlank()) {
            user.setName(command.getName());
        }

        if (command.getEmail() != null && !command.getEmail().isBlank()) {

            String newEmail = command.getEmail();

            if (!emailPattern.matcher(newEmail).matches()) {
                throw new BusinessException("user.email.invalid", newEmail);
            }

            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                userRepository.findByEmail(newEmail)
                        .ifPresent(u -> {
                            throw new BusinessException("user.email.exists", newEmail);
                        });
                user.setEmail(newEmail);
            }
        }

        if (command.getPassword() != null && !command.getPassword().isBlank()) {
            if (!passwordPattern.matcher(command.getPassword()).matches()) {
                throw new BusinessException("user.password.invalid");
            }
            user.setPassword(command.getPassword());
        }

        if (command.getActive() != null) {
            user.setActive(command.getActive());
        }

        if (command.getPhones() != null) {
            List<Phone> phones = command.getPhones()
                    .stream()
                    .map(p -> Phone.builder()
                            .number(p.getNumber())
                            .cityCode(p.getCityCode())
                            .countryCode(p.getCountryCode())
                            .build())
                    .toList();
            user.setPhones(phones);
        }

        user.setUpdatedAt(now);

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private UserResponse toResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId().toString());
        resp.setCreated(user.getCreatedAt());
        resp.setModified(user.getUpdatedAt());
        resp.setLastLogin(user.getLastLogin());
        resp.setToken(user.getToken());
        resp.setActive(user.isActive());
        resp.setName(user.getName());
        resp.setEmail(user.getEmail());

        if (user.getPhones() != null) {
            resp.setPhones(
                    user.getPhones().stream()
                            .map(p -> {
                                UserResponse.PhoneResponse pr = new UserResponse.PhoneResponse();
                                pr.setNumber(p.getNumber());
                                pr.setCityCode(p.getCityCode());
                                pr.setCountryCode(p.getCountryCode());
                                return pr;
                            }).toList()
            );
        }

        return resp;
    }
}
