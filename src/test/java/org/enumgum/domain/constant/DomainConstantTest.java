package org.enumgum.domain.constant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DomainConstantTest {

  @Test
  void testRole_ShouldContainThreeRoles() {
    assertEquals(3, Role.values().length);
    assertThat(Role.values()).containsExactlyInAnyOrder(Role.ADMIN, Role.MANAGER, Role.MEMBER);
  }

  @Test
  void testTokenType_ShouldContainThreeTokenTypes() {
    assertEquals(3, TokenType.values().length);
    assertThat(TokenType.values())
        .containsExactlyInAnyOrder(TokenType.VERIFICATION, TokenType.REFRESH, TokenType.INVITE);
  }

  @Test
  void testProgramStatus_ShouldContainFourProgramStatus() {
    assertEquals(4, ProgramStatus.values().length);
    assertThat(ProgramStatus.values())
        .containsExactlyInAnyOrder(
            ProgramStatus.PLANNED,
            ProgramStatus.ACTIVE,
            ProgramStatus.COMPLETED,
            ProgramStatus.ARCHIVED);
  }

  @Test
  void testPlan_ShouldContainThreePlans() {
    assertEquals(3, Plan.values().length);
    assertThat(Plan.values()).containsExactlyInAnyOrder(Plan.FREE, Plan.PRO, Plan.ENTERPRISE);
  }
}
