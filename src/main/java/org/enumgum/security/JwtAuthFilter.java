package org.enumgum.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
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

    if (jwt != null && tokenProvider.validateToken(jwt)) {
      Claims claims = tokenProvider.parseToken(jwt).getBody();
      //            String subject = claims.getSubject();
      UUID subject = UUID.fromString(claims.getSubject());
      String role = claims.get("role", String.class);
      //            UUID orgId = claims.get("org", UUID.class);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              subject, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    System.out.println("JwtAuthFilter: Processing request for URI " + request.getRequestURI());
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
