package org.enumgum.controller;

import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
  @GetMapping("/throw")
  public void throwEx() {
    throw new RuntimeException("boom!");
  }

  /* ---------- dummy controller ---------- */
  @RestController
  @RequestMapping("/api/dummy")
  static class DummyApi {
    @GetMapping("/me")
    public String me(@AuthenticationPrincipal UUID userId) {
      return userId.toString().equals("550e8400-e29b-41d4-a716-446655440000") ? "admin" : "user";
    }
  }
}
