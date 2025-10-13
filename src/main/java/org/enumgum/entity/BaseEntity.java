package org.enumgum.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}
