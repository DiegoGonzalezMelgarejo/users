package com.diego.interview.infraestructure;
import com.diego.interview.domain.model.User;
import com.diego.interview.infraestructure.out.persistence.entity.UserEntity;
import com.diego.interview.infraestructure.out.persistence.repository.UserJpaRepository;
import com.diego.interview.infraestructure.out.persistence.repository.UserRepositoryAdapter;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class userRepsotoryPortAdapterTest {

    @Mock
    private UserJpaRepository jpaRepository;

    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_shouldDelegateToJpaAndReturnMappedDomainUser() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        User domainUser = User.builder()
                .id(id)
                .name("John Doe")
                .email("john.doe@test.com")
                .password("secret")
                .createdAt(now)
                .updatedAt(now)
                .lastLogin(now)
                .active(true)
                .token("token-123")
                .build();

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(id.toString());
        savedEntity.setName("John Doe");
        savedEntity.setEmail("john.doe@test.com");
        savedEntity.setPassword("secret");
        savedEntity.setCreatedAt(now);
        savedEntity.setUpdatedAt(now);
        savedEntity.setLastLogin(now);
        savedEntity.setActive(true);
        savedEntity.setToken("token-123");

        when(jpaRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        User result = adapter.save(domainUser);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(jpaRepository, times(1)).save(captor.capture());
        UserEntity sentToJpa = captor.getValue();

        assertThat(sentToJpa.getEmail()).isEqualTo(domainUser.getEmail());
        assertThat(sentToJpa.getName()).isEqualTo(domainUser.getName());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findByEmail_shouldReturnMappedUserWhenFound() {
        String email = "john.doe@test.com";
        LocalDateTime now = LocalDateTime.now();

        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setName("John Doe");
        entity.setEmail(email);
        entity.setPassword("secret");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setLastLogin(now);
        entity.setActive(true);
        entity.setToken("token-123");

        when(jpaRepository.findByEmail(email)).thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findByEmail(email);

        verify(jpaRepository, times(1)).findByEmail(email);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenNotFound() {
        String email = "notfound@test.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<User> result = adapter.findByEmail(email);

        verify(jpaRepository, times(1)).findByEmail(email);
        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnMappedUserWhenFound() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        UserEntity entity = new UserEntity();
        entity.setId(id.toString());
        entity.setName("Jane Doe");
        entity.setEmail("jane.doe@test.com");
        entity.setPassword("secret");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setLastLogin(now);
        entity.setActive(true);
        entity.setToken("token-456");

        when(jpaRepository.findById(id.toString())).thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findById(id);

        verify(jpaRepository, times(1)).findById(id.toString());
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("jane.doe@test.com");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();

        when(jpaRepository.findById(id.toString())).thenReturn(Optional.empty());

        Optional<User> result = adapter.findById(id);

        verify(jpaRepository, times(1)).findById(id.toString());
        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_shouldCallJpaRepositoryWithStringId() {
        UUID id = UUID.randomUUID();

        adapter.deleteById(id);

        verify(jpaRepository, times(1)).deleteById(id.toString());
    }
}