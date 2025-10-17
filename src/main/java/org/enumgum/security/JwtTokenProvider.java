package org.enumgum.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProvider {

  @Value(
      "${app.jwt.secret:mySecretKeyMustBeAtLeast256BitsLongForHS512AlgorithmMySecretKeyMustBeAtLeast256BitsLongForHS512Algorithm}")
  private String secret;

  private static final long ACCESS_TOKEN_EXPIRY_SECONDS = 3600L; // 1 hour
  private static final long REFRESH_TOKEN_EXPIRY_SECONDS = 604800L; // 7 days

  @Override
  public String generateAccessToken(UUID userId, String email, UUID orgId, String role) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRY_SECONDS * 1000);

    Key signingKey = Keys.hmacShaKeyFor(secret.getBytes());

    return Jwts.builder()
        .setSubject(userId.toString()) // Use user ID as the subject
        .claim("email", email) // Add email as a custom claim
        .claim("org", orgId.toString())
        .claim("role", role)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(signingKey, SignatureAlgorithm.HS512) // Use HS512
        .compact();
  }

  @Override
  public String generateRefreshToken(UUID userId, UUID familyId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRY_SECONDS * 1000);

    Key signingKey = Keys.hmacShaKeyFor(secret.getBytes());

    // For refresh token rotation, you might want to include a 'jti' (JWT ID) or 'family' claim
    // The persistence and invalidation of the *old* token happens in the service layer.
    // Here, we just generate the token string.

    return Jwts.builder()
        .setSubject(userId.toString())
        .claim("family", familyId.toString())
        // Add a unique ID for the token instance if rotation logic requires it at the provider
        // level
        //         .claim("jti", UUID.randomUUID().toString())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(signingKey, SignatureAlgorithm.HS512)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      parseToken(token, secret);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public String rotateRefreshToken(String oldRefreshToken) {
    //        todo not yet implemented
    return "";
  }

  // Helper method for parsing tokens (used by validateToken and potentially tests)
  // Made package-private for testing access if needed, or keep private if only used internally
  Jws<Claims> parseToken(String token, String secret) {
    Key signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
  }

  String createTokenWithSpecificTimes(UUID userId, Date issuedAt, Date expiresAt, String secret) {

    Key signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.builder()
        .setSubject(userId.toString())
        .setIssuedAt(issuedAt)
        .setExpiration(expiresAt)
        .signWith(signingKey, SignatureAlgorithm.HS512)
        .compact();
  }
}
