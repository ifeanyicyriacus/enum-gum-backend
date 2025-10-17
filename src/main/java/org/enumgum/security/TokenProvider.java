package org.enumgum.security;

import java.util.UUID;

public interface TokenProvider {

  String generateAccessToken(UUID userId, String email, UUID orgId, String role);

  String generateRefreshToken(UUID userId, UUID family);

  boolean validateToken(String token);

  String rotateRefreshToken(String oldRefreshToken);
}
