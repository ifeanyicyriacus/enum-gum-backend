package org.enumgum.service;

import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.enumgum.domain.error.ErrorCode;
import org.enumgum.domain.model.VerificationToken;
import org.enumgum.dto.*;
import org.enumgum.entity.User;
import org.enumgum.exception.BusinessException;
import org.enumgum.repository.RefreshTokenRepository;
import org.enumgum.repository.UserRepository;
import org.enumgum.repository.VerificationTokenRepository;
import org.enumgum.security.TokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional // Ensure database operations are atomic
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

  private UserRepository userRepository;

  private RefreshTokenRepository refreshTokenRepository;

  private VerificationTokenRepository verificationTokenRepository;

  private PasswordEncoder passwordEncoder;

  private TokenProvider tokenProvider; // Might be used for generating verification token string

  @Override
  public SignupResponse signup(SignupRequest request) {
    String email = request.email();

    // Check if user exists and is verified -> Conflict
    Optional<User> existingUserOpt = userRepository.findByEmail(email);
    if (existingUserOpt.isPresent()) {
      User existingUser = existingUserOpt.get();
      if (existingUser.getVerified()) { // Assuming 'verified' is a boolean field in User
        throw new BusinessException(ErrorCode.EMAIL_IN_USE, "Email already in use and verified.");
      } else {
        // User exists but is not verified -> Update password, regenerate token, resend email
        User existingUserToUpdate = existingUserOpt.get();
        existingUserToUpdate.setPassword(passwordEncoder.encode(request.password()));

        // Regenerate verification token
        String newVerificationToken =
            generateVerificationToken(existingUserToUpdate.getId(), email);
        VerificationToken tokenEntity = new VerificationToken();
        tokenEntity.setToken(newVerificationToken);
        tokenEntity.setEmail(email);
        tokenEntity.setExpiresAt(Instant.now().plusSeconds(24 * 3600)); // 24 hours expiry
        tokenEntity.setUsed(false);
        // Set other fields (id, timestamps) via BaseEntity if applicable

        // Save updated user and new token
        userRepository.save(existingUserToUpdate);
        verificationTokenRepository.save(tokenEntity);

        // TODO: Resend verification email using email service (stubbed for now)
        // emailService.sendVerificationEmail(email, newVerificationToken);

        return new SignupResponse("Verification email resent. Please check your inbox.");
      }
    }

    // User does not exist -> Create new user
    User newUser =
        User.builder()
            .email(email)
            .password(passwordEncoder.encode(request.password()))
            .verified(false) // User is not verified initially
            .build();

    User savedUser = userRepository.save(newUser);

    // Generate verification token
    String verificationToken = generateVerificationToken(savedUser.getId(), email);
    VerificationToken tokenEntity = new VerificationToken();
    tokenEntity.setToken(verificationToken);
    tokenEntity.setEmail(email);
    tokenEntity.setExpiresAt(Instant.now().plusSeconds(24 * 3600)); // 24 hours expiry
    tokenEntity.setUsed(false);
    // Set other fields (id, timestamps) via BaseEntity if applicable

    verificationTokenRepository.save(tokenEntity);

    // TODO: Send verification email using email service (stubbed for now)
    // emailService.sendVerificationEmail(email, verificationToken);

    return new SignupResponse("Verification email sent successfully. Please check your inbox.");
  }

  @Override
  public TokenResponse login(LoginRequest req) {
    String email = req.email();
    String password = req.password();

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.AUTHENTICATION_ERROR, "Invalid credentials"));

    if (!user.getVerified()) {
      throw new BusinessException(
          ErrorCode.EMAIL_NOT_VERIFIED, "Email not verified"); // Email not verified
    }
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new BusinessException(ErrorCode.AUTHENTICATION_ERROR, "Invalid credentials");
    }

    String access =
        tokenProvider.generateAccessToken(
            user.getId(), user.getEmail() /*, UUID.randomUUID(), "MEMBER"*/);
    String refresh =
        tokenProvider.generateRefreshToken(user.getId(), UUID.randomUUID()); // family created here
    return new TokenResponse(access, refresh, "Bearer", 3600); // 1 hour in seconds);
  }

  @Override
  public TokenResponse refresh(RefreshRequest req) {
    if (!tokenProvider.validateToken(req.refreshToken())) {
      throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Invalid refresh token");
    }
    Claims claims = tokenProvider.parseToken(req.refreshToken()).getBody();
    UUID userId = UUID.fromString(claims.getSubject());
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.AUTHENTICATION_ERROR, "Invalid credentials"));

    // rotate: same family, new iat/exp
    String newAccess =
        tokenProvider.generateAccessToken(userId, user.getEmail() /*, UUID.randomUUID()*/);
    String newRefresh = tokenProvider.rotateRefreshToken(req.refreshToken());
    return new TokenResponse(newAccess, newRefresh, "Bearer", 3600); // 1 hour in seconds
  }

  @Override
  public void logout(LogoutRequest req) {
    if (!tokenProvider.validateToken(req.refreshToken())) return; // silent fail
    Claims claims = tokenProvider.parseToken(req.refreshToken()).getBody();
    UUID userId = UUID.fromString(claims.getSubject());
    refreshTokenRepository.deleteByToken(req.refreshToken()); // simple delete
  }

  // Helper method to generate verification token string
  // You might use the TokenProvider or just generate a random UUID string
  private String generateVerificationToken(UUID userId, String email) {
    // Option 1: Use TokenProvider (if adapted for verification tokens)
    // return tokenProvider.generateVerificationToken(userId, email); // Requires TokenProvider
    // change
    // Option 2: Generate random UUID string (simpler for verification tokens)
    return UUID.randomUUID().toString();
  }
}
