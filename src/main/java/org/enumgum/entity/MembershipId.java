package org.enumgum.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record MembershipId(UUID userId, UUID orgId) implements Serializable {}
