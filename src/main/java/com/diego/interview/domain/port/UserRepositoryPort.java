package com.diego.interview.domain.port;

import com.diego.interview.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);

    void deleteById(UUID id);
    List<User> findAllPaged(int page, int size);

    long countAll();

}
