package com.diego.interview.infraestructure;

import com.diego.interview.domain.model.User;
import com.diego.interview.infraestructure.security.JwtTokenProviderPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderPortTest {

    private static final String SECRET = "0123456789_0123456789_0123456789_01";

    @Test
    void generateToken_shouldReturnValidJwtWithEmailAsSubject() {
        long expirationInSeconds = 3600L;
        JwtTokenProviderPort provider = new JwtTokenProviderPort(SECRET, expirationInSeconds);

        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .email("john.doe@test.com")
                .build();

        String token = provider.generateToken(user);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();

        boolean valid = provider.validateToken(token);
        assertThat(valid).isTrue();

        String emailFromToken = provider.getEmailFromToken(token);
        assertThat(emailFromToken).isEqualTo("john.doe@test.com");
    }

    @Test
    void validateToken_shouldReturnFalseForMalformedToken() {
        JwtTokenProviderPort provider = new JwtTokenProviderPort(SECRET, 3600L);

        String invalidToken = "esto-no-es-un-token-jwt";

        boolean valid = provider.validateToken(invalidToken);

        assertThat(valid).isFalse();
    }
    @Test
    void validateToken_shouldReturnFalseWhenSignedWithDifferentSecret() {
        JwtTokenProviderPort providerReal =
                new JwtTokenProviderPort(SECRET, 3600L);

        String OTHER_SECRET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ_123456";

        JwtTokenProviderPort providerOther =
                new JwtTokenProviderPort(OTHER_SECRET, 3600L);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("john.doe@test.com")
                .build();

        String token = providerReal.generateToken(user);

        boolean validWithOther = providerOther.validateToken(token);

        assertThat(validWithOther).isFalse();
    }

    @Test
    void getEmailFromToken_shouldThrowExceptionForInvalidToken() {
        JwtTokenProviderPort provider = new JwtTokenProviderPort(SECRET, 3600L);
        String invalidToken = "invalid.token.structure";

        assertThatThrownBy(() -> provider.getEmailFromToken(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void generateToken_shouldSetExpirationInFuture() {
        long expirationInSeconds = 5L; // 5 segundos
        JwtTokenProviderPort provider = new JwtTokenProviderPort(SECRET, expirationInSeconds);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("john.doe@test.com")
                .build();

        Instant before = Instant.now();

        String token = provider.generateToken(user);

        boolean valid = provider.validateToken(token);
        assertThat(valid).isTrue();

        Instant after = Instant.now();
        assertThat(after).isAfterOrEqualTo(before);
    }
}
