package com.diego.interview.application;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.application.usecase.impl.LoginUserUseCaseImpl;
import com.diego.interview.domain.exception.BusinessException;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private PasswordEncoderPort passwordEncoder;   // 👈 nuevo mock

    private LoginUserUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        // 👇 ahora inyectamos también el encoder
        useCase = new LoginUserUseCaseImpl(userRepository, tokenProvider, passwordEncoder);
    }

    @Test
    void login_shouldReturnUserResponseAndUpdateLastLoginAndToken_whenCredentialsAreValid() {
        String email = "john.doe@test.com";
        String rawPassword = "secret";
        String encodedPassword = "encoded-secret";
        UUID id = UUID.randomUUID();
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime updated = LocalDateTime.now().minusHours(2);
        LocalDateTime oldLastLogin = LocalDateTime.now().minusHours(5);

        User user = User.builder()
                .id(id)
                .name("John Doe")
                .email(email)
                .password(encodedPassword)   // 🔐 guardado en BD como hash
                .createdAt(created)
                .updatedAt(updated)
                .lastLogin(oldLastLogin)
                .active(true)
                .token(null)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // el encoder debe decir que la contraseña es correcta
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(tokenProvider.generateToken(user)).thenReturn("new-jwt-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = useCase.login(email, rawPassword);

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
        verify(tokenProvider, times(1)).generateToken(user);
        verify(userRepository, times(1)).save(any(User.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // lastLogin actualizado y token seteado
        assertThat(savedUser.getLastLogin()).isNotNull();
        assertThat(savedUser.getLastLogin()).isAfter(oldLastLogin);
        assertThat(savedUser.getToken()).isEqualTo("new-jwt-token");

        // response correcto
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id.toString());
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.isActive()).isTrue();
        assertThat(response.getToken()).isEqualTo("new-jwt-token");
        assertThat(response.getCreated()).isEqualTo(created);
        assertThat(response.getModified()).isEqualTo(updated);
        assertThat(response.getLastLogin()).isEqualTo(savedUser.getLastLogin());
    }

    @Test
    void login_shouldThrowBusinessException_whenUserNotFound() {
        String email = "unknown@test.com";
        String rawPassword = "whatever";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.login(email, rawPassword)
        );

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(0)).matches(any(), any());
        verify(tokenProvider, times(0)).generateToken(any());
        verify(userRepository, times(0)).save(any());

        assertThat(ex.getCode()).isEqualTo("user.login.invalidCredentials");
        assertThat(ex.getArgs()).containsExactly(email);
    }

    @Test
    void login_shouldThrowBusinessException_whenPasswordIsInvalid() {
        String email = "john.doe@test.com";
        String rawPassword = "badPassword";
        String encodedPassword = "encoded-secret";

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(encodedPassword)
                .active(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // 👇 el encoder indica que la contraseña NO coincide
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.login(email, rawPassword)
        );

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
        verify(tokenProvider, times(0)).generateToken(any());
        verify(userRepository, times(0)).save(any());

        assertThat(ex.getCode()).isEqualTo("user.login.invalidCredentials");
        assertThat(ex.getArgs()).containsExactly(email);
    }
}