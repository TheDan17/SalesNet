package com.thedan17.salesnet.core.object.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Класс для хранения в БД связей между {@code Account} и {@code Group}.
 *
 * <p>Использует аннотации jakarta для хранения в {@code AccGroupLinkService} (наследник {@code
 * JpaRepository<AccGroupLink, Long>})
 */
@Entity
@Getter
@Setter
@Table(
    name = "acc_group_link",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"account_id", "group_id"})})
@AllArgsConstructor
@NoArgsConstructor
public class AccGroupLink {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  private Group group;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  private String inviteCode;

  private final LocalDateTime linkedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
}
