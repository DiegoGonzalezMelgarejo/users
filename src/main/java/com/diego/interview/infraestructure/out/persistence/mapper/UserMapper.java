package com.diego.interview.infraestructure.out.persistence.mapper;

import com.diego.interview.domain.model.Phone;
import com.diego.interview.domain.model.User;
import com.diego.interview.infraestructure.out.persistence.entity.PhoneEntity;
import com.diego.interview.infraestructure.out.persistence.entity.UserEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserMapper{
    public static UserEntity toEntity(User user) {
        if (user == null) return null;

        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setLastLogin(user.getLastLogin());
        entity.setToken(user.getToken());
        entity.setActive(user.isActive());

        // Map phones
        List<PhoneEntity> phoneEntities =
                user.getPhones() == null ? List.of() :
                        user.getPhones().stream()
                                .map(p -> toEntityPhone(p, entity))
                                .collect(Collectors.toList());

        entity.setPhones(phoneEntities);

        return entity;
    }

    private static PhoneEntity toEntityPhone(Phone phone, UserEntity parent) {
        PhoneEntity entity = new PhoneEntity();

        entity.setId(phone.getId());
        entity.setNumber(phone.getNumber());
        entity.setCityCode(phone.getCityCode());
        entity.setCountryCode(phone.getCountryCode());

        entity.setUser(parent);

        return entity;
    }


    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;

        User user =  User.builder()
                .id(UUID.fromString(entity.getId()))
                .name(entity.getName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lastLogin(entity.getLastLogin())
                .token(entity.getToken())
                .active(entity.isActive())
                .build();

        if (entity.getPhones() != null) {
            List<Phone> phones = entity.getPhones()
                    .stream()
                    .map(UserMapper::toDomainPhone)
                    .collect(Collectors.toList());
            user.setPhones(phones);
        }

        return user;
    }
    private static Phone toDomainPhone(PhoneEntity entity) {
        return Phone.builder()
                .id(entity.getId())
                .number(entity.getNumber())
                .cityCode(entity.getCityCode())
                .countryCode(entity.getCountryCode())
                .build();
    }
}
