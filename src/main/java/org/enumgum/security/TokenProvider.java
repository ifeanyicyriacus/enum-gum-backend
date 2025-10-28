package org.enumgum.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import java.util.UUID;

public interface TokenProvider {

  String generateAccessToken(UUID userId, String email/*, UUID orgId, String role*/);

  String generateRefreshToken(UUID userId, UUID family);

  boolean validateToken(String token);

  String rotateRefreshToken(String oldRefreshToken);

  Jws<Claims> parseToken(String token);

  String createTokenWithSpecificTimes(UUID userId, Date issuedAt, Date expiresAt, String secret);

  Claims getClaimsIfValid(String token)
      throws ExpiredJwtException, MalformedJwtException, SignatureException;
}
