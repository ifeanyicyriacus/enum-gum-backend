package org.enumgum.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.enumgum.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
// @RequiredArgsConstructor
public class TokenProvider2 {

  @Value(
      "${app.jwt.secret:mySecretKeyMustBeAtLeast256BitsLongForHS512AlgorithmMySecretKeyMustBeAtLeast256BitsLongForHS512Algorithm}")
  private String secret;

  private static final long ACCESS_TOKEN_EXPIRY_SECONDS = 3600L; // 1 hour
  private static final long REFRESH_TOKEN_EXPIRY_SECONDS = 604800L; // 7 days

  public String generateAccessToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRY_SECONDS * 1000);

    Key signingKey = Keys.hmacShaKeyFor(secret.getBytes());

    return Jwts.builder()
        .setSubject(user.getId().toString()) // Use user ID as the subject
        .claim("email", user.getEmail()) // Add email as a custom claim
        // Add other claims if needed (e.g., role)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(signingKey, SignatureAlgorithm.HS512) // Use HS512
        .compact();
  }

  // Helper method for parsing tokens (used by validateToken and potentially tests)
  // Made package-private for testing access if needed, or keep private if only used internally
  Jws<Claims> parseToken(String token, String secret) {
    Key signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
  }

  public String generateRefreshToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRY_SECONDS * 1000);

    Key signingKey = Keys.hmacShaKeyFor(secret.getBytes());

    // For refresh token rotation, you might want to include a 'jti' (JWT ID) or 'family' claim
    // The persistence and invalidation of the *old* token happens in the service layer.
    // Here, we just generate the token string.
    return Jwts.builder()
        .setSubject(user.getId().toString())
        .claim("email", user.getEmail())
        // Add a unique ID for the token instance if rotation logic requires it at the provider
        // level
        // .claim("jti", UUID.randomUUID().toString())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(signingKey, SignatureAlgorithm.HS512)
        .compact();
  }

  public Claims validateToken(String token) {
    try {
      Jws<Claims> claimsJws = parseToken(token, secret);
      return claimsJws.getBody();
    } catch (ExpiredJwtException e) {
      // Log the error if needed
      throw e; // Re-throw to be handled by the caller/service layer
    } catch (MalformedJwtException e) {
      // Log the error if needed
      throw e;
    } catch (SignatureException e) {
      // Log the error if needed
      throw e;
    } catch (IllegalArgumentException e) {
      // Log the error if needed
      throw e;
    }
  }

  String createTokenWithSpecificTimes(User user, Date issuedAt, Date expiresAt, String secret) {

    Key signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.builder()
        .setSubject(user.getId().toString())
        .claim("email", user.getEmail())
        .setIssuedAt(issuedAt)
        .setExpiration(expiresAt)
        .signWith(signingKey, SignatureAlgorithm.HS512)
        .compact();
  }

  //  public String generateAccessToken(UUID userId, String email, UUID orgId, String role) {
  //
  //    Instant now = clock.instant();
  //    return Jwts.builder()
  //        .setSubject(userId.toString())
  //        .claim("email", email)
  //        .claim("org", orgId.toString())
  //        .claim("role", role)
  //        .setIssuedAt(Date.from(now))
  //        .setExpiration(Date.from(now.plusSeconds(15 * 60))) // 15 min
  //        .signWith(key(), SignatureAlgorithm.HS256)
  //        .compact();
  //  }
  //
  //  @Override
  //  public String generateRefreshToken(UUID userId, UUID family) {
  //    Instant now = clock.instant();
  //    return Jwts.builder()
  //        .setSubject(userId.toString())
  //        .claim("family", family.toString())
  //        .setIssuedAt(Date.from(now))
  //        .setExpiration(Date.from(now.plus(Duration.ofDays(7))))
  //        .signWith(key(), SignatureAlgorithm.HS256)
  //        .compact();
  //  }

  //  @Override
  //  public boolean validateToken(String token) {
  //    try {
  //      Jwts.parserBuilder()
  //          .setSigningKey(key())
  //          .setClock(() -> Date.from(clock.instant()))
  //          .build()
  //          .parseClaimsJws(token);
  //      return true;
  //    } catch (JwtException | IllegalArgumentException ex) {
  //      return false;
  //    }
  //  }
  //
  //  @Override
  //  public String rotateRefreshToken(String oldToken) {
  //    try {
  //      Claims claims =
  //          Jwts.parserBuilder()
  //              .setSigningKey(key())
  //              .setClock(() -> Date.from(clock.instant()))
  //              .build()
  //              .parseClaimsJws(oldToken)
  //              .getBody();
  //
  //      UUID family = UUID.fromString(claims.get("family", String.class));
  //      UUID userId = UUID.fromString(claims.getSubject());
  //      return generateRefreshToken(userId, family);
  //    } catch (Exception ex) {
  //      throw new IllegalArgumentException("Invalid refresh token", ex);
  //    }
  //  }
}
