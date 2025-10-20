package org.enumgum.controller.advice.web;

import org.enumgum.security.JwtSecurityMockConfig;
import org.enumgum.security.WithMockJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DemoControllerTest.DummyApi.class)
@Import(JwtSecurityMockConfig.class)
public class DemoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockJwt(userId = "550e8400-e29b-41d4-a716-446655440000", role = "ADMIN")
    void shouldAllowAccessWithMockJwt() throws Exception {
        mvc.perform(get("/api/dummy/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("admin"));
    }

    @Test
    void shouldDenyAccessWithoutJwt() throws Exception {
        mvc.perform(get("/api/dummy/me"))
                .andExpect(status().isUnauthorized());
    }

    /* ---------- dummy controller ---------- */
    @RestController
    @RequestMapping("/api/dummy")
    static class DummyApi {
        @GetMapping("/me")
        public String me(@AuthenticationPrincipal UUID userId) {
            return userId.toString()
                    .equals("550e8400-e29b-41d4-a716-446655440000") ? "admin" : "user";
        }
    }


}
