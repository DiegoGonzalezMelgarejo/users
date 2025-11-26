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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    // 🔽 NUEVOS TESTS 🔽

    @Test
    void findAllPaged_shouldReturnMappedUsersList() {
        int page = 1;
        int size = 2;
        LocalDateTime now = LocalDateTime.now();

        UserEntity e1 = new UserEntity();
        e1.setId(UUID.randomUUID().toString());
        e1.setName("User 1");
        e1.setEmail("user1@test.com");
        e1.setPassword("secret");
        e1.setCreatedAt(now);
        e1.setUpdatedAt(now);
        e1.setLastLogin(now);
        e1.setActive(true);
        e1.setToken("t1");

        UserEntity e2 = new UserEntity();
        e2.setId(UUID.randomUUID().toString());
        e2.setName("User 2");
        e2.setEmail("user2@test.com");
        e2.setPassword("secret");
        e2.setCreatedAt(now);
        e2.setUpdatedAt(now);
        e2.setLastLogin(now);
        e2.setActive(true);
        e2.setToken("t2");

        when(jpaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(e1, e2)));

        var result = adapter.findAllPaged(page, size);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository, times(1)).findAll(pageableCaptor.capture());

        Pageable usedPageable = pageableCaptor.getValue();
        assertThat(usedPageable.getPageNumber()).isEqualTo(page);
        assertThat(usedPageable.getPageSize()).isEqualTo(size);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("user1@test.com");
        assertThat(result.get(1).getEmail()).isEqualTo("user2@test.com");
    }

    @Test
    void findAllPaged_shouldReturnEmptyListWhenNoUsers() {
        int page = 0;
        int size = 5;

        when(jpaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var result = adapter.findAllPaged(page, size);

        verify(jpaRepository, times(1)).findAll(any(Pageable.class));
        assertThat(result).isEmpty();
    }

    @Test
    void countAll_shouldDelegateToJpaCount() {
        when(jpaRepository.count()).thenReturn(5L);

        long result = adapter.countAll();

        verify(jpaRepository, times(1)).count();
        assertThat(result).isEqualTo(5L);
    }
}
