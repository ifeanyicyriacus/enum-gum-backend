package org.enumgum.service;

import org.enumgum.dto.*;

public interface AuthService {
  SignupResponse signup(SignupRequest request);

  TokenResponse login(LoginRequest request);

  TokenResponse refresh(RefreshRequest req);

  void logout(String refreshToken);

}
