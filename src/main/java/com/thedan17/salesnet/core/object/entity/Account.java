package com.thedan17.salesnet.core.object.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import lombok.Data;

/**
 * Внутренний класс для представления аккаунта пользователя.
 *
 * <p>В будущем у каждого {@code Account} будет несколько {@code User}, поле Type должно быть enum
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
  private String type;
  private LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

  //@OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  //private Set<AccGroupLink> members = new HashSet<>();

  /** Дефолтный конструктор для создания связей. */
  public Account() {
    // Just default constructor
  }

  /** Получение объектов {@code Group} из связей. */
  /*@Transient
  public List<Group> getGroups() {
    return Collections.unmodifiableList(
            members.stream()
                    .map(AccGroupLink::getGroup)
                    .toList()
    );
  }*/
}
