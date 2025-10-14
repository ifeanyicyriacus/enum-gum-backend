package org.enumgum.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.enumgum.entity.BaseEntity;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private Instant expiresAt;

  @Builder.Default
  @Column(nullable = false)
  private boolean used = false;

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }
}
