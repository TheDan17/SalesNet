package com.thedan17.salesnet.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "groups")
public class Group {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;
  private LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<AccGroupLink> members = new HashSet<>();

  /** Конструктор для совместимости с компонентами сервиса. */
  public Group(String name, String description) {
    this.name = name;
    this.description = description;
  }

  /** Стандартный конструктор для совместимости со Spring. */
  public Group() {}

  /**
   * Метод для косвенного добавления {@code Account} в {@code Group}.
   *
   * @deprecated Уже устаревший метод с сомнительной полезностью
   */
  public AccGroupLink addAccount(Account account) {
    AccGroupLink link = new AccGroupLink();
    link.setAccount(account);
    link.setGroup(this);
    this.members.add(link);
    account.getMembers().add(link);
    return link;
  }

  /**
   * Метод для кастомного получения {@code Account}-участников, путём перебора связей.
   *
   * @deprecated Так же устарел, как и {@code addAccount(...)}
   */
  public Set<Account> getAccounts() {
    Set<Account> accounts = new HashSet<>(Set.of());
    for (AccGroupLink member : this.members) {
      accounts.add(member.getAccount());
    }
    return accounts;
  }
}
