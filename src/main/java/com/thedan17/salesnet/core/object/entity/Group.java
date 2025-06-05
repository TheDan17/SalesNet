package com.thedan17.salesnet.core.object.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/** Сущность для представления существующей группы, в которому могут вступать {@code Account}. */
@Entity
@Data
@Table(
    name = "groups",
    indexes = {@Index(columnList = "customId", unique = true)})
@AllArgsConstructor
public class Group {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ToString.Exclude private String customId;
  private String name;
  @ToString.Exclude private String description;

  @ToString.Exclude
  private LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @ToString.Exclude
  private Set<AccGroupLink> members = new HashSet<>();

  private Long ownerId;

  /** Конструктор инициализации для Spring. */
  public Group(String name, String description) {
    this.name = name;
    this.description = description;
  }

  /** Дефолтный конструктор для совместимости со Spring. */
  public Group() {
    // just default constructor
  }
}
