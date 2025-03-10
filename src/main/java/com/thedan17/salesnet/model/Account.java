package com.thedan17.salesnet.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 * Внутренний класс для представления аккаунта пользователя.
 *
 * <p>{@code TODO}: в будущем у каждого {@code Account} будет несколько {@code User}
 */
@Data
@Entity
@Table(
    name = "accounts",
    indexes = {@Index(columnList = "login", unique = true)})
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // service data
  private String login;
  private String email;
  private String passwordHash;

  // info data
  private String firstName;
  private String secondName;
  private String type; // TODO it should be not String in future
  private LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<AccGroupLink> members = new HashSet<>();

  /** Получение объектов {@code Group} из связей. */
  public List<Group> getGroups() {
    List<Group> groups = new ArrayList<>();
    for (AccGroupLink member : this.members) {
      groups.add(member.getGroup());
    }
    return groups;
  }
}
