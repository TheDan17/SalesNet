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

  /** Дефолтный конструктор для создания связей. */
  public Account() {
    // Just default constructor
  }
}
