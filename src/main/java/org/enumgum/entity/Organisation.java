package org.enumgum.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enumgum.domain.constant.Plan;

@Entity
@Table(name = "organisations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organisation extends BaseEntity {

  @Column(nullable = false, length = 120)
  private String name;

  private String logoUrl;

  private String website;

  private String industry;

  @Column(length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private Plan plan = Plan.FREE;
}
