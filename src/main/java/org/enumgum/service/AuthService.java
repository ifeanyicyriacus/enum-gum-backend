package org.enumgum.service;

import org.enumgum.dto.SignupRequest;
import org.enumgum.dto.SignupResponse;

public interface AuthService {
  SignupResponse signup(SignupRequest request);
}
