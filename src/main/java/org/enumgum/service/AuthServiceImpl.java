package org.enumgum.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.enumgum.domain.error.ErrorCode;
import org.enumgum.domain.model.RefreshToken;
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
            generateVerificationToken(/*existingUserToUpdate.getId(), email*/ );
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

//    User savedUser = userRepository.save(newUser);
    userRepository.save(newUser);

    // Generate verification token
    String verificationToken = generateVerificationToken(/*savedUser.getId(), email*/ );
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
  public TokenResponse refresh(String refreshToken) {

    RefreshToken tokenEntity =
        refreshTokenRepository
            .findByToken(refreshToken)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.TOKEN_INVALID, "Refresh token not found"));
    // Step 2: Check if the token is already used (reused token detection)
    if (tokenEntity.isUsed()) {
      // PRD requirement: Re-use of old refresh token -> revoke entire family
      // Mark all tokens in the same family as used.
      refreshTokenRepository.markFamilyUsed(tokenEntity.getFamily());
      throw new BusinessException(
          ErrorCode.TOKEN_REUSE_DETECTED, "Token reuse detected. Family revoked.");
    }

    // Step 3: Check if the token is expired
    // (redundant check if JWT expiry is also checked, but good for DB consistency)
    if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
      throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "Refresh token expired");
    }

    // Step 4: Validate the JWT string itself (signature, expiry)
    Claims claims;
    try {
      claims = tokenProvider.parseToken(refreshToken).getBody();
    } catch (ExpiredJwtException
        | MalformedJwtException
        | SignatureException
        | UnsupportedJwtException
        | IllegalArgumentException e) {
      throw new BusinessException(
          ErrorCode.TOKEN_INVALID, "Invalid refresh token format or signature");
    }

    UUID userId = UUID.fromString(claims.getSubject()); // Subject should be the user ID
    UUID familyId =
        UUID.fromString(claims.get("family", String.class)); // Get family ID from claims

    // Step 6: Verify the family ID in the JWT matches the one in the DB entity
    // This is a secondary check, but the findByToken lookup already ensures the entity matches the
    // token string.
    // If the family ID in the JWT payload is different from the DB entity, it's a mismatch.
    // However, the primary lookup is by token string. The family ID in the JWT is used for reuse
    // detection above.
    // We can rely on the DB lookup by token string being authoritative for the user ID and family
    // ID for this instance.
    // If the token string is valid and not used/revoked, we can proceed.
    // If the family ID from the DB entity doesn't match the one in the JWT claim, it's an error
    // state.
    // Let's assume they should match for consistency.
    if (!tokenEntity.getFamily().equals(familyId)) {
      throw new BusinessException(ErrorCode.TOKEN_INVALID, "Token family mismatch");
    }

    // Step 7: Fetch the user from the database using the user ID from the JWT claims
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        ErrorCode.TOKEN_INVALID, "Invalid or expired verification token"));

    // Step 8: Generate new access token
    String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail());

    // Step 9:
    // Generate a new refresh token (potentially with the same family ID, or a new one if full
    // rotation is needed)
    // For "family rotation", we keep the same family ID but invalidate the old token.
    String newRefreshTokenString = tokenProvider.generateRefreshToken(user.getId(), familyId);

    // Step 10: Mark the *old* refresh token entity as used
    tokenEntity.setUsed(true);
    refreshTokenRepository.save(tokenEntity);

    RefreshToken newRefreshTokenEntity =
        RefreshToken.builder()
            .token(newRefreshTokenString)
            .userId(user.getId())
            .family(familyId) // Same family for family rotation concept
            .expiresAt(Instant.now().plusSeconds(7 * 24 * 3600)) // 7 days expiry
            .used(false)
            .build();

    refreshTokenRepository.save(newRefreshTokenEntity);

    return new TokenResponse(newAccessToken, newRefreshTokenString, "Bearer", 3600);
  }

  @Override
  public void logout(String refreshToken) {
    Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);

    if (tokenOpt.isPresent()) {
      RefreshToken tokenEntity = tokenOpt.get();
      tokenEntity.setUsed(true);
      refreshTokenRepository.save(tokenEntity);
    }
  }

  @Override
  public void verifyEmail(String verificationToken) {
    // Step 1: Find the verification token entity by its string value
    VerificationToken tokenEntity =
        verificationTokenRepository
            .findByToken(verificationToken)
            .orElseThrow(
                () ->
                    new BusinessException(ErrorCode.TOKEN_INVALID, "Verification token not found"));

    // Step 2: Check if the token is already used
    if (tokenEntity.isUsed()) {
      throw new BusinessException(
          ErrorCode.TOKEN_REUSE_DETECTED, "Verification token already used");
    }

    // Step 3: Check if the token is expired
    if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
      throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "Verification token expired");
    }

    // Step 4: Find the user associated with the token's email
    User user =
        userRepository
            .findByEmail(tokenEntity.getEmail())
            .orElseThrow(
                () ->
                    new BusinessException(
                        ErrorCode.TOKEN_INVALID, "Invalid or expired verification token"));

    // Step 5: Update the user's verification status
    user.setVerified(true); // Set verified flag to true
    userRepository.save(user); // Save the updated user

    // Step 6: Mark the verification token as used
    tokenEntity.setUsed(true);
    verificationTokenRepository.save(tokenEntity); // Save the updated token
  }

  // Helper method to generate verification token string
  // You might use the TokenProvider or just generate a random UUID string
  private String generateVerificationToken(/*UUID userId, String email*/ ) {
    // Option 1: Use TokenProvider (if adapted for verification tokens)
    // return tokenProvider.generateVerificationToken(userId, email); // Requires TokenProvider
    // change
    // Option 2: Generate random UUID string (simpler for verification tokens)
    return UUID.randomUUID().toString();
  }
}
