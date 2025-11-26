package com.diego.interview.infraestructure.out.persistence.repository;

import com.diego.interview.domain.port.UserRepositoryPort;
import com.diego.interview.domain.model.User;
import com.diego.interview.infraestructure.out.persistence.entity.UserEntity;
import com.diego.interview.infraestructure.out.persistence.mapper.UserMapper;

import java.util.Optional;
import java.util.UUID;

import static com.diego.interview.infraestructure.out.persistence.mapper.UserMapper.toDomain;
import static com.diego.interview.infraestructure.out.persistence.mapper.UserMapper.toEntity;

public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpa;

    public UserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id.toString()).map(UserMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id.toString());
    }

}
