package org.enumgum.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class WithMockJwtSecurityContextFactory implements
        WithSecurityContextFactory<WithMockJwt> {

    private static final String MOCK_SECRET =
            "mockSecretKeyMustBeAtLeast256BitsLongForHS512Algorithm" +
                    "MockSecretKeyMustBeAtLeast256BitsLongForHS512Algorithm";

    @Override
    public SecurityContext createSecurityContext(WithMockJwt annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        SecretKey key = Keys.hmacShaKeyFor(MOCK_SECRET.getBytes());
        String jwt = Jwts.builder()
                .setSubject(annotation.userId())
                .claim("email", annotation.email())
                .claim("org", annotation.orgId())
                .claim("role", annotation.role())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // place the JWT string as credentials so controllers can read it if needed
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        UUID.fromString(annotation.userId()),   // principal
                        jwt,                                      // credentials (the mock JWT)
                        List.of(new SimpleGrantedAuthority("ROLE_" + annotation.role())));

        context.setAuthentication(auth);
        return context;
    }
}
