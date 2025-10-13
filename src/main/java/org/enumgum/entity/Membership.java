package org.enumgum.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.enumgum.domain.constant.Role;

@Entity
@Table(name = "memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// @IdClass(MembershipId.class)
public class Membership {

  @EmbeddedId private MembershipId id;

  @MapsId("userId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @MapsId("orgId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "org_id", insertable = false, updatable = false)
  private Organisation organisation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = updatedAt = Instant.now();
    if (this.id == null && this.user != null && this.organisation != null) {
      this.id = new MembershipId(user.getId(), organisation.getId());
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}
