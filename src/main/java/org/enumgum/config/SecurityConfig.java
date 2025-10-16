package org.enumgum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
// @EnableConfigurationProperties(JwtSecretConfig.class)
public class SecurityConfig {

  @Bean
  SecurityFilterChain chain(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(a -> a.anyRequest().permitAll())
        .addFilterBefore(new TraceFilter(), UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  //  @Bean
  //  public TokenProvider tokenProvider(JwtSecretConfig cfg) {
  //    return new JwtTokenProvider(cfg, Clock.systemUTC());
  //  }
}
