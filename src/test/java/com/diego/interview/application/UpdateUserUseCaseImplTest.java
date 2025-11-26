package com.diego.interview.application;

import com.diego.interview.application.usecase.dto.UpdateUserCommand;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.application.usecase.impl.UpdateUserUseCaseImpl;
import com.diego.interview.domain.exception.BusinessException;
import com.diego.interview.domain.model.Phone;
import com.diego.interview.domain.model.User;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    private Pattern emailPattern;
    private Pattern passwordPattern;

    private UpdateUserUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        emailPattern = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
        passwordPattern = Pattern.compile("^.{8,}$"); // mínimo 8 caracteres

        useCase = new UpdateUserUseCaseImpl(userRepository, emailPattern, passwordPattern);
    }

    @Test
    void update_shouldUpdateUserFieldsAndReturnResponse_whenDataIsValid() {
        UUID id = UUID.randomUUID();
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime updated = LocalDateTime.now().minusHours(2);

        User existing = User.builder()
                .id(id)
                .name("Old Name")
                .email("old.email@test.com")
                .password("oldPassword")
                .createdAt(created)
                .updatedAt(updated)
                .lastLogin(updated)
                .active(true)
                .phones(List.of(
                        Phone.builder().number("1111111111").cityCode("1").countryCode("57").build()
                ))
                .token("old-token")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("new.email@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserCommand command = UpdateUserCommand.builder()
                .name("New Name")
                .email("new.email@test.com")
                .password("newPassword123")
                .active(false)
                .phones(List.of(
                        UpdateUserCommand.PhoneCommand.builder()
                                .number("2222222222")
                                .cityCode("2")
                                .countryCode("34")
                                .build()
                ))
                .build();

        UserResponse response = useCase.update(id, command);

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).findByEmail("new.email@test.com");
        verify(userRepository, times(1)).save(any(User.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getName()).isEqualTo("New Name");
        assertThat(saved.getEmail()).isEqualTo("new.email@test.com");
        assertThat(saved.getPassword()).isEqualTo("newPassword123");
        assertThat(saved.isActive()).isFalse();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getPhones()).hasSize(1);
        assertThat(saved.getPhones().get(0).getNumber()).isEqualTo("2222222222");

        assertThat(response.getId()).isEqualTo(id.toString());
        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getEmail()).isEqualTo("new.email@test.com");
        assertThat(response.isActive()).isFalse();
        assertThat(response.getPhones()).hasSize(1);
        assertThat(response.getPhones().get(0).getNumber()).isEqualTo("2222222222");
    }

    @Test
    void update_shouldThrowBusinessException_whenUserNotFound() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.update(id, UpdateUserCommand.builder().build())
        );

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(0)).save(any(User.class));

        assertThat(ex.getCode()).isEqualTo("user.notFound");
        assertThat(ex.getArgs()).containsExactly(id.toString());
    }

    @Test
    void update_shouldThrowBusinessException_whenEmailFormatIsInvalid() {
        UUID id = UUID.randomUUID();

        User existing = User.builder()
                .id(id)
                .email("old.email@test.com")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        UpdateUserCommand command = UpdateUserCommand.builder()
                .email("invalid-email-format") // no cumple regex
                .build();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.update(id, command)
        );

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(0)).findByEmail("invalid-email-format");
        verify(userRepository, times(0)).save(any(User.class));

        assertThat(ex.getCode()).isEqualTo("user.email.invalid");
        assertThat(ex.getArgs()).containsExactly("invalid-email-format");
    }

    @Test
    void update_shouldThrowBusinessException_whenEmailAlreadyExists() {
        UUID id = UUID.randomUUID();

        User existing = User.builder()
                .id(id)
                .email("old.email@test.com")
                .build();

        User another = User.builder()
                .id(UUID.randomUUID())
                .email("new.email@test.com")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("new.email@test.com")).thenReturn(Optional.of(another));

        UpdateUserCommand command = UpdateUserCommand.builder()
                .email("new.email@test.com")
                .build();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.update(id, command)
        );

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).findByEmail("new.email@test.com");
        verify(userRepository, times(0)).save(any(User.class));

        assertThat(ex.getCode()).isEqualTo("user.email.exists");
        assertThat(ex.getArgs()).containsExactly("new.email@test.com");
    }

    @Test
    void update_shouldThrowBusinessException_whenPasswordDoesNotMatchPattern() {
        UUID id = UUID.randomUUID();

        User existing = User.builder()
                .id(id)
                .email("old.email@test.com")
                .password("oldPassword")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        UpdateUserCommand command = UpdateUserCommand.builder()
                .password("short")
                .build();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.update(id, command)
        );

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(0)).save(any(User.class));

        assertThat(ex.getCode()).isEqualTo("user.password.invalid");
    }
}
