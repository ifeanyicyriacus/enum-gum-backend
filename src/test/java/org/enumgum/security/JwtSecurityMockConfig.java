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

  //    @Bean
  //    @Primary // Make sure this mock takes precedence if a real one exists in the minimal context
  //    public TokenProvider mockTokenProvider() {
  //        // You can stub methods here if the filter's execution path during this test
  //        // might call them. Often, if the @WithMockJwt sets the context correctly,
  //        // the filter might not call validateToken, but it's good practice to mock.
  //        // Example stubbing (adjust based on your TokenProvider interface):
  //        // when(mockProvider.validateToken(anyString())).thenReturn(true);
  //        // when(mockProvider.getClaims(anyString())).thenReturn(mock(Claims.class)); // Requires
  // mocking Claims too
  //        // For now, just provide the mock bean.
  //        return Mockito.mock(TokenProvider.class);
  //    }
}
