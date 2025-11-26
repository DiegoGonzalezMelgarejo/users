package com.diego.interview.domain.port;

import com.diego.interview.domain.model.User;

public interface TokenProviderPort {
    String generateToken(User user);

    boolean validateToken(String token);

    String getEmailFromToken(String token);
}
