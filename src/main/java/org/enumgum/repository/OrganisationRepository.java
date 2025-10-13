package org.enumgum.repository;

import java.util.UUID;
import org.enumgum.entity.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {}
