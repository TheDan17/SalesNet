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
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

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

  private String customId;
  private String name;
  private String description;
  private LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<AccGroupLink> members = new HashSet<>();

  /** Конструктор инициализации для Spring. */
  public Group(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
