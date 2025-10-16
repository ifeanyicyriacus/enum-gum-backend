// package org.enumgum.security;
//
// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.JwtException;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;
// import java.nio.charset.StandardCharsets;
// import java.time.Clock;
// import java.time.Duration;
// import java.time.Instant;
// import java.util.Date;
// import java.util.UUID;
// import javax.crypto.SecretKey;
// import lombok.RequiredArgsConstructor;
// import org.enumgum.config.JwtSecretConfig;
//
// @RequiredArgsConstructor
// public class JwtTokenProvider implements TokenProvider {
//
//  private final JwtSecretConfig cfg;
//  private final Clock clock;
//
//  private SecretKey key() {
//    return Keys.hmacShaKeyFor(cfg.secret().getBytes(StandardCharsets.UTF_8));
//  }
//
//  @Override
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
//
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
// }
