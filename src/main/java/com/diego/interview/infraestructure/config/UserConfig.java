package com.diego.interview.infraestructure.config;

import com.diego.interview.domain.port.TokenProviderPort;
import com.diego.interview.domain.port.UserRepositoryPort;
import com.diego.interview.application.usecase.CreateUserUseCase;
import com.diego.interview.application.usecase.impl.CreateUserUseCaseImpl;
import com.diego.interview.infraestructure.out.persistence.mapper.UserMapper;
import com.diego.interview.infraestructure.out.persistence.repository.UserJpaRepository;
import com.diego.interview.infraestructure.out.persistence.repository.UserRepositoryAdapter;
import com.diego.interview.infraestructure.security.JwtTokenProviderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.regex.Pattern;

@Configuration
public class UserConfig {

    @Bean
    public UserMapper userMapper() {
        return new UserMapper();
    }

    @Bean
    public UserRepositoryPort userRepository(UserJpaRepository jpa) {
        return new UserRepositoryAdapter(jpa);
    }


    @Bean
    public CreateUserUseCase createUserUseCase(
            UserRepositoryPort userRepositoryPort,
            Pattern emailPattern,
            Pattern passwordPattern,
            TokenProviderPort tokenProviderPort) {

        return new CreateUserUseCaseImpl(
                userRepositoryPort,
                emailPattern,
                passwordPattern,
                tokenProviderPort
        );
    }
    @Bean
    public TokenProviderPort tokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds}") long expiration) {
        return new JwtTokenProviderPort(secret, expiration);
    }
}
