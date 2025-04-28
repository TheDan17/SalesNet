package com.thedan17.salesnet.core.object.entity;

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
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.ToString;

/** Сущность для представления существующей группы, в которому могут вступать {@code Account}. */
@Entity
@Data
@Table(
    name = "groups",
    indexes = {@Index(columnList = "customId", unique = true)})
public class Group {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ToString.Exclude private String customId;
  private String name;
  @ToString.Exclude private String description;

  @ToString.Exclude
  private LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private Set<AccGroupLink> members = new HashSet<>();

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
