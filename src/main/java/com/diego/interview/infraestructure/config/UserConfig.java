package com.diego.interview.infraestructure.config;

import com.diego.interview.application.usecase.DeleteUserUseCase;
import com.diego.interview.application.usecase.GetUserByIdUseCase;
import com.diego.interview.application.usecase.ListUsersUseCase;
import com.diego.interview.application.usecase.LoginUserUseCase;
import com.diego.interview.application.usecase.UpdateUserUseCase;
import com.diego.interview.application.usecase.impl.UpdateUserUseCaseImpl;
import com.diego.interview.application.usecase.impl.DeleteUserUseCaseImpl;
import com.diego.interview.application.usecase.impl.GetUserByIdUseCaseImpl;
import com.diego.interview.application.usecase.impl.ListUsersUseCaseImpl;
import com.diego.interview.application.usecase.impl.LoginUserUseCaseImpl;
import com.diego.interview.domain.port.PasswordEncoderPort;
import com.diego.interview.domain.port.TokenProviderPort;
import com.diego.interview.domain.port.UserRepositoryPort;
import com.diego.interview.application.usecase.CreateUserUseCase;
import com.diego.interview.application.usecase.impl.CreateUserUseCaseImpl;
import com.diego.interview.infraestructure.out.persistence.mapper.UserMapper;
import com.diego.interview.infraestructure.out.persistence.repository.UserJpaRepository;
import com.diego.interview.infraestructure.out.persistence.repository.UserRepositoryAdapter;
import com.diego.interview.infraestructure.security.BCryptPasswordEncoderAdapter;
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
            TokenProviderPort tokenProviderPort,
            PasswordEncoderPort passwordEncoderPort) {

        return new CreateUserUseCaseImpl(
                userRepositoryPort,
                emailPattern,
                passwordPattern,
                tokenProviderPort,
                passwordEncoderPort
        );
    }
    @Bean
    public GetUserByIdUseCase getUserByIdUseCase(UserRepositoryPort userRepositoryPort){
        return new GetUserByIdUseCaseImpl(userRepositoryPort);
    }
    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepositoryPort userRepositoryPort){
        return new DeleteUserUseCaseImpl(userRepositoryPort);
    }
    @Bean
    public UpdateUserUseCase updateUserUseCase(UserRepositoryPort userRepositoryPort,Pattern emailPattern,
                                               Pattern passwordPattern){
        return new UpdateUserUseCaseImpl(userRepositoryPort, emailPattern,passwordPattern);
    }
    @Bean
    public LoginUserUseCase  loginUserUseCase(
            UserRepositoryPort userRepositoryPort,
            TokenProviderPort tokenProviderPort,
            PasswordEncoderPort passwordEncoderPort) {

        return new LoginUserUseCaseImpl(
                userRepositoryPort,
                tokenProviderPort,
                passwordEncoderPort
        );
    }
    @Bean
    public PasswordEncoderPort passwordEncoderPort() {
        return new BCryptPasswordEncoderAdapter();
    }
    @Bean
    public ListUsersUseCase listUsersUseCase(
            UserRepositoryPort userRepositoryPort) {

        return new ListUsersUseCaseImpl(
                userRepositoryPort
        );
    }
    @Bean
    public TokenProviderPort tokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds}") long expiration) {
        return new JwtTokenProviderPort(secret, expiration);
    }
}
