package org.enumgum.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record MembershipId(UUID userId, UUID orgId) implements Serializable {

  //    @Override
  //    public boolean equals(Object o) {
  //        if (this == o) return true;
  //        if (o == null || getClass() != o.getClass()) return false;
  //        MembershipId that = (MembershipId) o;
  //        return userId.equals(that.userId) && orgId.equals(that.orgId);
  //    }
  //
  //    @Override
  //    public int hashCode() {
  //        return 31 * userId.hashCode() + orgId.hashCode();
  //    }
}
