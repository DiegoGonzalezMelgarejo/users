package com.diego.interview.application;

import com.diego.interview.application.usecase.dto.PagedResponse;
import com.diego.interview.application.usecase.dto.UserResponse;
import com.diego.interview.application.usecase.impl.ListUsersUseCaseImpl;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUsersUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    private ListUsersUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListUsersUseCaseImpl(userRepository);
    }

    @Test
    void listUsers_shouldReturnPagedResponseWithMappedUsers() {
        int page = 1;
        int size = 2;
        LocalDateTime now = LocalDateTime.now();

        User u1 = User.builder()
                .id(UUID.randomUUID())
                .name("User 1")
                .email("user1@test.com")
                .createdAt(now)
                .updatedAt(now)
                .lastLogin(now)
                .token("t1")
                .active(true)
                .phones(List.of(
                        Phone.builder().number("1111111111").cityCode("1").countryCode("57").build()
                ))
                .build();

        User u2 = User.builder()
                .id(UUID.randomUUID())
                .name("User 2")
                .email("user2@test.com")
                .createdAt(now)
                .updatedAt(now)
                .lastLogin(now)
                .token("t2")
                .active(false)
                .phones(List.of())
                .build();

        when(userRepository.findAllPaged(page, size)).thenReturn(List.of(u1, u2));
        when(userRepository.countAll()).thenReturn(5L); // total elementos

        PagedResponse<UserResponse> response = useCase.listUsers(page, size);

        verify(userRepository, times(1)).findAllPaged(page, size);
        verify(userRepository, times(1)).countAll();

        assertThat(response).isNotNull();
        assertThat(response.getPage()).isEqualTo(page);
        assertThat(response.getSize()).isEqualTo(size);
        assertThat(response.getTotalElements()).isEqualTo(5L);
        assertThat(response.getTotalPages()).isEqualTo(3);

        List<UserResponse> content = response.getContent();
        assertThat(content).hasSize(2);

        UserResponse r1 = content.get(0);
        assertThat(r1.getName()).isEqualTo("User 1");
        assertThat(r1.getEmail()).isEqualTo("user1@test.com");
        assertThat(r1.isActive()).isTrue();
        assertThat(r1.getToken()).isEqualTo("t1");
        assertThat(r1.getPhones()).hasSize(1);
        assertThat(r1.getPhones().get(0).getNumber()).isEqualTo("1111111111");

        UserResponse r2 = content.get(1);
        assertThat(r2.getName()).isEqualTo("User 2");
        assertThat(r2.getEmail()).isEqualTo("user2@test.com");
        assertThat(r2.isActive()).isFalse();
        assertThat(r2.getPhones()).isEmpty();
    }

    @Test
    void listUsers_shouldNormalizeNegativePageAndZeroSize() {
        int page = -3;
        int size = 0;
        int expectedPage = 0;
        int expectedSize = 10;

        when(userRepository.findAllPaged(expectedPage, expectedSize)).thenReturn(List.of());
        when(userRepository.countAll()).thenReturn(0L);

        PagedResponse<UserResponse> response = useCase.listUsers(page, size);

        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(userRepository, times(1)).findAllPaged(pageCaptor.capture(), sizeCaptor.capture());
        verify(userRepository, times(1)).countAll();

        assertThat(pageCaptor.getValue()).isEqualTo(expectedPage);
        assertThat(sizeCaptor.getValue()).isEqualTo(expectedSize);

        assertThat(response.getPage()).isEqualTo(expectedPage);
        assertThat(response.getSize()).isEqualTo(expectedSize);
        assertThat(response.getTotalElements()).isEqualTo(0L);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    void listUsers_shouldHandleEmptyResultList() {
        int page = 0;
        int size = 5;

        when(userRepository.findAllPaged(page, size)).thenReturn(List.of());
        when(userRepository.countAll()).thenReturn(0L);

        PagedResponse<UserResponse> response = useCase.listUsers(page, size);

        verify(userRepository, times(1)).findAllPaged(page, size);
        verify(userRepository, times(1)).countAll();

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0L);
        assertThat(response.getTotalPages()).isEqualTo(0);
    }
}
