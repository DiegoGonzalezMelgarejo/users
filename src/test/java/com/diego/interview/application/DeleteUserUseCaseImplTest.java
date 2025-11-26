package com.diego.interview.application.usecase.impl;

import com.diego.interview.domain.exception.BusinessException;
import com.diego.interview.domain.model.User;
import com.diego.interview.domain.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    private DeleteUserUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteUserUseCaseImpl(userRepository);
    }

    @Test
    void deleteById_shouldDeleteWhenUserExists() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.of(User.builder().id(id).build()));

        assertDoesNotThrow(() -> useCase.deleteById(id));

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteById_shouldThrowBusinessExceptionWhenUserNotFound() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.deleteById(id)
        );

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(0)).deleteById(id);

        assertThat(ex.getCode()).isEqualTo("user.notFound");
        assertThat(ex.getArgs()).isNotNull();
        assertThat(ex.getArgs()).containsExactly(id.toString());
    }
}
