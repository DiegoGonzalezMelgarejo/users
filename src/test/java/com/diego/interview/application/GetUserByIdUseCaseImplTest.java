package com.diego.interview.application;

import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.application.usecase.impl.GetUserByIdUseCaseImpl;
import com.diego.interview.domain.exception.BusinessException;
import com.diego.interview.domain.model.Phone;
import com.diego.interview.domain.model.User;
import com.diego.interview.domain.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserByIdUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    private GetUserByIdUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUserByIdUseCaseImpl(userRepository);
    }

    @Test
    void getById_shouldReturnMappedUserResponseWhenUserExists() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Phone phone = Phone.builder()
                .number("1234567890")
                .cityCode("1")
                .countryCode("57")
                .build();

        User user = User.builder()
                .id(id)
                .name("John Doe")
                .email("john.doe@test.com")
                .password("secret")
                .createdAt(now)
                .updatedAt(now)
                .lastLogin(now)
                .active(true)
                .token("token-123")
                .phones(List.of(phone))
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponse response = useCase.getById(id);

        verify(userRepository, times(1)).findById(id);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id.toString());
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john.doe@test.com");
        assertThat(response.isActive()).isTrue();
        assertThat(response.getToken()).isEqualTo("token-123");
        assertThat(response.getCreated()).isEqualTo(now);
        assertThat(response.getModified()).isEqualTo(now);
        assertThat(response.getLastLogin()).isEqualTo(now);

        assertThat(response.getPhones()).hasSize(1);
        UserResponse.PhoneResponse phoneResp = response.getPhones().get(0);
        assertThat(phoneResp.getNumber()).isEqualTo("1234567890");
        assertThat(phoneResp.getCityCode()).isEqualTo("1");
        assertThat(phoneResp.getCountryCode()).isEqualTo("57");
    }

    @Test
    void getById_shouldThrowBusinessExceptionWhenUserNotFound() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.getById(id)
        );

        verify(userRepository, times(1)).findById(id);

        assertThat(ex.getCode()).isEqualTo("user.notFound");
        assertThat(ex.getArgs()).isNotNull();
        assertThat(ex.getArgs()).containsExactly(id.toString());
    }
}
