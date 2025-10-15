package org.enumgum.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.enumgum.entity.BaseEntity;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private UUID family;

  @Column(nullable = false)
  private Instant expiresAt;

  @Builder.Default
  @Column(nullable = false)
  private boolean used = false;
}
