package org.enumgum.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// import org.springframework.stereotype.Service;

// @Service
@Component
public class JwtTokenProvider implements TokenProvider {

  @Value(
      "${app.jwt.secret:mySecretKeyMustBeAtLeast256BitsLongForHS512AlgorithmMySecretKeyMustBeAtLeast256BitsLongForHS512Algorithm}")
  private String secret;

  private static final long ACCESS_TOKEN_EXPIRY_SECONDS = 3600L; // 1 hour
  private static final long REFRESH_TOKEN_EXPIRY_SECONDS = 604800L; // 7 days
  private final AtomicLong counter = new AtomicLong();

  @Override
  public String generateAccessToken(UUID userId, String email, UUID orgId, String role) {
    Date now = new Date();
    Date expiryDate = Date.from(now.toInstant().plusSeconds(ACCESS_TOKEN_EXPIRY_SECONDS));
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
    Date expiryDate = Date.from(now.toInstant().plusSeconds(REFRESH_TOKEN_EXPIRY_SECONDS));

    Key signingKey = Keys.hmacShaKeyFor(secret.getBytes());

    return Jwts.builder()
        .setSubject(userId.toString())
        .claim("family", familyId.toString())
        .claim("jti", counter.incrementAndGet())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(signingKey, SignatureAlgorithm.HS512)
        .compact();
  }

  @Override
  public boolean validateToken(String token) {
    try {
      parseToken(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public String rotateRefreshToken(String oldRefreshToken) {
    try {
      Claims claims = parseToken(oldRefreshToken).getBody();
      UUID userId = UUID.fromString(claims.getSubject());
      UUID family = UUID.fromString(claims.get("family", String.class));

      return generateRefreshToken(userId, family);
    } catch (JwtException | IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid refresh token", ex);
    }
  }

  @Override
  public Jws<Claims> parseToken(String token) {
    Key signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
  }

  @Override
  public String createTokenWithSpecificTimes(
      UUID userId, Date issuedAt, Date expiresAt, String secret) {

    Key signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.builder()
        .setSubject(userId.toString())
        .setIssuedAt(issuedAt)
        .setExpiration(expiresAt)
        .signWith(signingKey, SignatureAlgorithm.HS512)
        .compact();
  }

  @Override
  public Claims getClaimsIfValid(String token)
      throws ExpiredJwtException, MalformedJwtException, SignatureException {
    return parseToken(token).getBody();
  }
}
