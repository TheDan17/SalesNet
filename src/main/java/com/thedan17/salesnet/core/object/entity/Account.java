package com.thedan17.salesnet.core.object.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@AllArgsConstructor
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
  @Column(nullable = true)
  private String secondName;
  private String type;
  private LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

  /** Дефолтный конструктор для создания связей. */
  public Account() {
    // Just default constructor
  }
}
