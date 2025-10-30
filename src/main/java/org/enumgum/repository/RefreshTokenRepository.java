package org.enumgum.repository;

import jakarta.validation.constraints.NotBlank;
import java.util.Optional;
import java.util.UUID;
import org.enumgum.domain.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByToken(String token);

  Optional<RefreshToken> findByFamily(UUID family);

  @Modifying
  @Query("update RefreshToken r set r.used = true where r.family = ?1")
  void markFamilyUsed(UUID family);

  void deleteByToken(@NotBlank String token);
}
