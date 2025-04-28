package com.thedan17.salesnet.core.object.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.Setter;

/**
 * Класс для хранения в БД связей между {@code Account} и {@code Group}.
 *
 * <p>Использует аннотации jakarta для хранения в {@code AccGroupLinkRepository} (наследник {@code
 * JpaRepository<AccGroupLink, Long>})
 */
@Entity
@Getter
@Setter
@Table(
    name = "acc_group_link",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"account_id", "group_id"})})
public class AccGroupLink {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "group_id")
  private Group group;

  @ManyToOne
  @JoinColumn(name = "account_id")
  private Account account;

  private final LocalDateTime linkedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

}
