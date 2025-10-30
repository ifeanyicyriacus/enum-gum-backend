package org.enumgum.controller.web;

import jakarta.validation.Valid;
import org.enumgum.dto.*;
import org.enumgum.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/signup")
  public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
    SignupResponse response = authService.signup(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED); // Return 201
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    TokenResponse response = authService.login(request);
    return new ResponseEntity<>(response, HttpStatus.OK); // Return 200 OK
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@RequestHeader("Authorization") String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String refreshToken = authHeader.substring(7);
      TokenResponse response = authService.refresh(refreshToken);
      return new ResponseEntity<>(response, HttpStatus.OK);
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String refreshToken = authHeader.substring(7);
      authService.logout(refreshToken);
    }
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/verify-email")
  public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
    authService.verifyEmail(token);
    return ResponseEntity.ok().build();
  }
}
