package org.enumgum.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI enumAPI() {
    return new OpenAPI().info(new Info().title("Enum Gum Organisation API").version("v1"));
  }
}
