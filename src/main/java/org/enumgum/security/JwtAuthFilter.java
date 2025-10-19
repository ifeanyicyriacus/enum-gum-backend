package org.enumgum.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

  private final TokenProvider tokenProvider;

  public JwtAuthFilter(TokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String jwt = parseJwt(request);

    if (jwt != null) {
      try {
        if (tokenProvider.validateToken(jwt)) {
          Claims claims = tokenProvider.parseToken(jwt).getBody();
          //            String subject = claims.getSubject();
          UUID subject = UUID.fromString(claims.getSubject());
          String role = claims.get("role", String.class);
          //            UUID orgId = claims.get("org", UUID.class);

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  subject, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

          SecurityContextHolder.getContext().setAuthentication(authentication);
          logger.debug("Set authentication for user: {}", subject);
        } else {
          logger.debug("Jwt validation failed for token: {}", jwt);
        }
      } catch (ExpiredJwtException
          | MalformedJwtException
          | SignatureException
          | UnsupportedJwtException
          | IllegalArgumentException e) {
        logger.error("Could not set user authentication from JWT token: {}", jwt, e);
      }
    } else {
      logger.debug("No JWT token found in request headers");
    }

    filterChain.doFilter(request, response);
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7); // Remove "Bearer " prefix
    }

    return null;
  }
}
