package org.enumgum.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockJwtSecurityContextFactory implements WithSecurityContextFactory<WithMockJwt> {

  private static final String MOCK_SECRET =
      "mockSecretKeyMustBeAtLeast256BitsLongForHS512Algorithm"
          + "MockSecretKeyMustBeAtLeast256BitsLongForHS512Algorithm";

  @Override
  public SecurityContext createSecurityContext(WithMockJwt annotation) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    String userId = annotation.userId();
    String email = annotation.email();
    String role = annotation.role();
    String orgId = annotation.orgId();

    SecretKey key = Keys.hmacShaKeyFor(MOCK_SECRET.getBytes());
    String jwt =
        Jwts.builder()
            .setSubject(userId)
            .claim("email", email)
            .claim("org", role)
            .claim("role", orgId)
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();

    // place the JWT string as credentials so controllers can read it if needed
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            UUID.fromString(userId), // principal
            jwt, // credentials (the mock JWT)
            List.of(new SimpleGrantedAuthority("ROLE_" + role)));

    context.setAuthentication(auth);
    return context;
  }
}
