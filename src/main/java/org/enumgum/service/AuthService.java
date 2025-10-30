package org.enumgum.service;

import org.enumgum.dto.*;

public interface AuthService {
  SignupResponse signup(SignupRequest request);

  TokenResponse login(LoginRequest request);

  TokenResponse refresh(String refreshToken);

  void logout(String refreshToken);

  void verifyEmail(String verificationToken);
}
