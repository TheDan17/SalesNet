package com.thedan17.salesnet.service;

import com.thedan17.salesnet.dao.AccountRepository;
import com.thedan17.salesnet.dto.AccountInfoDto;
import com.thedan17.salesnet.dto.AccountLoginDto;
import com.thedan17.salesnet.dto.AccountUpdateDto;
import com.thedan17.salesnet.dto.GroupIdDto;
import com.thedan17.salesnet.model.Account;
import jakarta.persistence.criteria.Predicate;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Представитель сервисного слоя для взаимодействия с сущностями {@code Account}. */
@Service
public class AccountService {
  @Autowired private final AccountRepository dao;
  @Autowired private final MapperService mapperService;

  /** Временный метод для хеширования пароля по методу SHA-256. */
  private static String hashWithSha256(String data) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(data.getBytes());
    StringBuilder hexString = new StringBuilder();
    for (byte b : hashBytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  /** Конструктор для автопривязки необходимых классов. */
  public AccountService(AccountRepository repository, MapperService mapperService) {
    this.dao = repository;
    this.mapperService = mapperService;
  }

  /** Метод создания и добавления {@code Account} в бд по информации пользователя. */
  @Transactional
  public Optional<AccountInfoDto> addAccount(AccountLoginDto accountLoginDto) {
    Account account = mapperService.loginDtoToAccount(accountLoginDto);
    try {
      account.setPasswordHash(hashWithSha256(accountLoginDto.getPassword()));
    } catch (NoSuchAlgorithmException e) {
      account.setPasswordHash("");
    }
    try {
      account = dao.save(account);
    } catch (DataIntegrityViolationException e) {
      return Optional.empty();
    }
    return Optional.of(mapperService.accountToInfoDto(account));
  }

  /** Внутренний метод получения {@code Account} из бд по id. */
  @Transactional
  private Optional<Account> getAccountEntityById(Long id) {
    Account account;
    try {
      account = dao.findById(id).orElseThrow();
    } catch (Exception e) {
      return Optional.empty();
    }
    return Optional.of(account);
  }

  /** Метод получения списка групп в виде DTO, в которых состоит запрашиваемый {@code Account}. */
  @Transactional
  public Optional<List<GroupIdDto>> getAccountGroups(Long id) {
    Optional<Account> account = getAccountEntityById(id);
    if (account.isEmpty()) {
      return Optional.empty();
    }
    List<GroupIdDto> groupIdDtoList = new ArrayList<>();
    for (var member : account.get().getMembers()) {
      groupIdDtoList.add(mapperService.groupToIdDto(member.getGroup()));
    }
    return Optional.of(groupIdDtoList);
  }

  /** Метод получения информации о {@code Account} из бд по id. */
  @Transactional
  public Optional<AccountInfoDto> getAccountById(Long id) {
    Account account;
    try {
      account = dao.findById(id).orElseThrow();
    } catch (Exception e) {
      return Optional.empty();
    }
    return Optional.of(mapperService.accountToInfoDto(account));
  }

  /** Метод фильтрации всех {@code Account} из бд по опциональным критериям. */
  @Transactional
  public List<AccountInfoDto> searchAccounts(
      String firstName, String secondName, String type) {
    Specification<Account> spec =
        (root, query, criteriaBuilder) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (firstName != null) {
            predicates.add(criteriaBuilder.equal(root.get("firstName"), firstName));
          }
          if (secondName != null) {
            predicates.add(criteriaBuilder.equal(root.get("secondName"), secondName));
          }
          if (type != null) {
            predicates.add(criteriaBuilder.equal(root.get("type"), type));
          }
          return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    List<Account> accountList = dao.findAll(spec);
    List<AccountInfoDto> accountInfoDtoList = new ArrayList<>();
    for (var account : accountList) {
      accountInfoDtoList.add(mapperService.accountToInfoDto(account));
    }
    return accountInfoDtoList;
  }

  /** Метод обновления информации о Account, но только разрешённых полей путём отдельного DTO. */
  @Transactional
  public Optional<AccountInfoDto> updateAccount(Long id, AccountUpdateDto updatedAccount) {
    Account account;
    try {
      account = dao.findById(id).orElseThrow();
    } catch (Exception e) {
      return Optional.empty();
    }
    Account updatedAccountEntity = mapperService.updateDtoToAccount(updatedAccount);
    account.setFirstName(updatedAccountEntity.getFirstName());
    account.setSecondName(updatedAccountEntity.getSecondName());
    account.setType(updatedAccount.getType());
    account = dao.save(account);
    return Optional.of(mapperService.accountToInfoDto(account));
  }

  /** Метод удаления Account, по его id. */
  @Transactional
  public Boolean deleteAccount(Long id) {
    if (this.getAccountEntityById(id).isPresent()) {
      dao.deleteById(id);
      return true;
    }
    return false;
  }
}
