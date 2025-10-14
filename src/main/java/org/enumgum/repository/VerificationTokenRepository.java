package org.enumgum.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.enumgum.domain.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
  Optional<VerificationToken> findByToken(String token);

  Optional<VerificationToken> findByEmail(String email);

  @Modifying
  @Query("delete from VerificationToken v where v.expiresAt < ?1")
  void deleteOlderThan(Instant now);
}
