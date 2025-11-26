package com.diego.interview.application;

import com.diego.interview.application.usecase.dto.CreateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.application.usecase.impl.CreateUserUseCaseImpl;
import com.diego.interview.domain.exception.BusinessException;
import com.diego.interview.domain.model.Phone;
import com.diego.interview.domain.model.User;
import com.diego.interview.domain.port.PasswordEncoderPort;
import com.diego.interview.domain.port.TokenProviderPort;
import com.diego.interview.domain.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;   // 👈 NUEVO MOCK

    private Pattern emailPattern;
    private Pattern passwordPattern;

    private CreateUserUseCaseImpl service;

    @BeforeEach
    void setUp() {
        emailPattern = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
        passwordPattern = Pattern.compile("^.{8,}$");

        service = new CreateUserUseCaseImpl(
                userRepositoryPort,
                emailPattern,
                passwordPattern,
                tokenProviderPort,
                passwordEncoderPort       // 👈 INYECTAMOS ENCODER
        );
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        String email = "john.doe@test.com";
        String rawPassword = "Password123";
        String encodedPassword = "encoded-Password123";   // 👈 simulamos hash
        String name = "John Doe";

        CreateUserCommand.PhoneCommand phoneCommand =
                CreateUserCommand.PhoneCommand.builder()
                        .number("1234567")
                        .cityCode("1")
                        .countryCode("57")
                        .build();

        CreateUserCommand command = CreateUserCommand.builder()
                .email(email)
                .password(rawPassword)
                .name(name)
                .phones(List.of(phoneCommand))
                .build();

        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoderPort.encode(rawPassword)).thenReturn(encodedPassword); // 👈 mock encode
        when(tokenProviderPort.generateToken(any(User.class))).thenReturn("dummy-token");

        UUID generatedId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        User savedUser = User.builder()
                .id(generatedId)
                .name(name)
                .email(email)
                .password(encodedPassword) // 👈 en BD queda hasheada
                .phones(List.of(
                        Phone.builder()
                                .number("1234567")
                                .cityCode("1")
                                .countryCode("57")
                                .build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .lastLogin(now)
                .active(true)
                .token("dummy-token")
                .build();

        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = service.createUser(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort, times(1)).save(userCaptor.capture());
        verify(userRepositoryPort, times(1)).findByEmail(email);
        verify(passwordEncoderPort, times(1)).encode(rawPassword);        // 👈 se llamó al encoder
        verify(tokenProviderPort, times(1)).generateToken(any(User.class));

        User userSentToRepo = userCaptor.getValue();
        assertThat(userSentToRepo.getEmail()).isEqualTo(email);
        assertThat(userSentToRepo.getName()).isEqualTo(name);
        assertThat(userSentToRepo.getPhones()).hasSize(1);
        // 👇 comprobamos que no se guardó la raw password
        assertThat(userSentToRepo.getPassword()).isEqualTo(encodedPassword);

        assertThat(response.getId()).isEqualTo(generatedId.toString());
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo(name);
        assertThat(response.isActive()).isTrue();
        assertThat(response.getToken()).isEqualTo("dummy-token");
        assertThat(response.getPhones()).hasSize(1);
        assertThat(response.getPhones().get(0).getNumber()).isEqualTo("1234567");
    }

    @Test
    void createUser_shouldThrowBusinessException_whenEmailIsInvalid() {
        String invalidEmail = "invalid-email";
        CreateUserCommand command = CreateUserCommand.builder()
                .email(invalidEmail)
                .password("Password123")
                .name("John Doe")
                .phones(List.of())
                .build();

        assertThatThrownBy(() -> service.createUser(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("user.email.invalid");
                });

        verify(userRepositoryPort, never()).save(any());
        verify(userRepositoryPort, never()).findByEmail(anyString());
        verify(passwordEncoderPort, never()).encode(any());
        verify(tokenProviderPort, never()).generateToken(any());
    }

    @Test
    void createUser_shouldThrowBusinessException_whenPasswordIsInvalid() {
        String email = "john.doe@test.com";
        String invalidPassword = "short";

        CreateUserCommand command = CreateUserCommand.builder()
                .email(email)
                .password(invalidPassword)
                .name("John Doe")
                .phones(List.of())
                .build();

        assertThatThrownBy(() -> service.createUser(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("user.password.invalid");
                });

        verify(userRepositoryPort, never()).save(any());
        verify(userRepositoryPort, never()).findByEmail(anyString());
        verify(passwordEncoderPort, never()).encode(any());
        verify(tokenProviderPort, never()).generateToken(any());
    }

    @Test
    void createUser_shouldThrowBusinessException_whenEmailAlreadyExists() {
        String email = "john.doe@test.com";

        CreateUserCommand command = CreateUserCommand.builder()
                .email(email)
                .password("Password123")
                .name("John Doe")
                .phones(List.of())
                .build();

        User existing = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .name("Existing User")
                .build();

        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createUser(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("user.email.exists");
                });

        verify(userRepositoryPort, times(1)).findByEmail(email);
        verify(userRepositoryPort, never()).save(any());
        verify(passwordEncoderPort, never()).encode(any());
        verify(tokenProviderPort, never()).generateToken(any());
    }
}
