package org.enumgum.config;

import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
// @Component
@ConfigurationProperties(prefix = "app.jwt")
public record JwtSecretConfig(@Size(min = 32, max = 64) String secret) {}
