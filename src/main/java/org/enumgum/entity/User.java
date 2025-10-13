package org.enumgum.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

  @Column(unique = true, nullable = false, length = 320)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  @Builder.Default
  private Boolean verified = false;
}
