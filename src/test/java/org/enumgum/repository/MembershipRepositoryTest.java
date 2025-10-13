package org.enumgum.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.enumgum.domain.constant.Role;
import org.enumgum.entity.Membership;
import org.enumgum.entity.Organisation;
import org.enumgum.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MembershipRepositoryTest {
  @Autowired private UserRepository userRepo;
  @Autowired private OrganisationRepository orgRepo;
  @Autowired private MembershipRepository memberRepo;

  @Test
  void saveMembership() {
    User user = userRepo.save(User.builder().email("a@b.com").password("password").build());
    assertNotNull(user);
    Organisation org = orgRepo.save(Organisation.builder().name("Org").build());
    assertNotNull(org);
    Membership membership =
        memberRepo.save(
            Membership.builder().user(user).organisation(org).role(Role.MEMBER).build());
    assertNotNull(membership);

    assertThat(memberRepo.existsByOrganisationIdAndUserId(org.getId(), user.getId())).isTrue();
    assertEquals(1, memberRepo.countByOrganisationIdAndRole(org.getId(), Role.MEMBER));
    assertEquals(0, memberRepo.countByOrganisationIdAndRole(org.getId(), Role.ADMIN));
    assertEquals(0, memberRepo.countByOrganisationIdAndRole(org.getId(), Role.MANAGER));
  }
}
