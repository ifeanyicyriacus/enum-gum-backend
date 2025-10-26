package org.enumgum.controller.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.enumgum.controller.TestController;
import org.enumgum.security.JwtSecurityMockConfig;
import org.enumgum.security.TokenProvider;
import org.enumgum.security.WithMockJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TestController.class)
@Import(JwtSecurityMockConfig.class)
public class DemoControllerTest {

  @Autowired private MockMvc mvc;

  @MockitoBean private TokenProvider mockTokenProvider;

  @Test
  @WithMockJwt(userId = "550e8400-e29b-41d4-a716-446655440000", role = "ADMIN")
  void shouldAllowAccessWithMockJwt() throws Exception {
    mvc.perform(get("/api/dummy/me"))
        .andExpect(status().isOk())
        .andExpect(content().string("admin"));
  }

  @Test
  @WithAnonymousUser
  void shouldDenyAccessWithoutJwt() throws Exception {
    mvc.perform(get("/api/dummy/me")).andExpect(status().isUnauthorized());
  }
}
