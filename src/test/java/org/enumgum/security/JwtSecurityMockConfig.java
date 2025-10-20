package org.enumgum.security;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithSecurityContextFactory;


@TestConfiguration
public class JwtSecurityMockConfig {

    @Bean
    public WithSecurityContextFactory withMockJwtSecurityContextFactory() {
        return new WithMockJwtSecurityContextFactory();
    }
}
