package org.enumgum.repository;

import java.util.UUID;
import org.enumgum.domain.constant.Role;
import org.enumgum.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
  boolean existsByOrganisationIdAndUserId(UUID orgId, UUID userId);

  long countByOrganisationIdAndRole(UUID orgId, Role role);
}
