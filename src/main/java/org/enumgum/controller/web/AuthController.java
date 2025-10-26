package org.enumgum.controller.web;

import jakarta.validation.Valid;
import org.enumgum.dto.SignupRequest;
import org.enumgum.dto.SignupResponse;
import org.enumgum.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired private AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
    SignupResponse response = authService.signup(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED); // Return 201
  }

  // Other endpoints (login, logout, refresh) will be added later
}
