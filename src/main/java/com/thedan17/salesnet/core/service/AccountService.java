package com.thedan17.salesnet.core.service;

import com.thedan17.salesnet.core.dao.AccGroupLinkRepository;
import com.thedan17.salesnet.core.dao.AccountRepository;
import com.thedan17.salesnet.core.object.dto.AccountInfoDto;
import com.thedan17.salesnet.core.object.dto.AccountSignupDto;
import com.thedan17.salesnet.core.object.dto.AccountUpdateDto;
import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.object.entity.AccGroupLink;
import com.thedan17.salesnet.core.object.entity.Account;
import com.thedan17.salesnet.exception.ContentNotFoundException;
import com.thedan17.salesnet.exception.SuchElementExistException;
import com.thedan17.salesnet.util.CommonUtil;
import com.thedan17.salesnet.util.EntityMapper;
import jakarta.persistence.criteria.Predicate;
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
  @Autowired private final EntityMapper entityMapper;
  @Autowired private final AccGroupLinkRepository accGroupLinkRepository;

  /** Конструктор для автопривязки необходимых классов. */
  public AccountService(AccountRepository repository, EntityMapper entityMapper, AccGroupLinkRepository accGroupLinkRepository) {
    this.dao = repository;
    this.entityMapper = entityMapper;
    this.accGroupLinkRepository = accGroupLinkRepository;
  }

  /** Метод создания и добавления {@code Account} в бд по информации пользователя. */
  @Transactional
  public Optional<AccountInfoDto> addAccount(AccountSignupDto accountSignupDto) {
    Account account = entityMapper.loginDtoToAccount(accountSignupDto);
    if (account.getSecondName() == null){
      account.setSecondName("");
    }
    account.setPasswordHash(CommonUtil.hashWithSha256(accountSignupDto.getPassword()));
    try {
      account = dao.save(account);
    } catch (DataIntegrityViolationException e) {
      throw new SuchElementExistException("Create account error: " + e.getMessage());
    }
    return Optional.of(entityMapper.accountToInfoDto(account));
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
    List<AccGroupLink> links = accGroupLinkRepository.findByAccount(account.get());
    for (var member : links) {
      groupIdDtoList.add(entityMapper.groupToIdDto(member.getGroup()));
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
    return Optional.of(entityMapper.accountToInfoDto(account));
  }

  public Specification<Account> buildAccountSpecification(String firstName, String secondName, String type) {
    return (root, query, criteriaBuilder) -> {
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
  }

  /** Метод фильтрации всех {@link Account} из БД по критериям и возврат в публичном виде.
   *
   * <p>Если все критерии равны null, то вернёт пустой список. */
  @Transactional
  public List<AccountInfoDto> searchAccounts(
      String firstName, String secondName, String type) {
    if (firstName == null && secondName == null && type == null) {
      return new ArrayList<>();
    }
    Specification<Account> spec = buildAccountSpecification(firstName, secondName, type);
    List<Account> accountList = dao.findAll(spec);
    List<AccountInfoDto> accountInfoDtoList = new ArrayList<>();
    for (var account : accountList) {
      accountInfoDtoList.add(entityMapper.accountToInfoDto(account));
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
    Account updatedAccountEntity = entityMapper.updateDtoToAccount(updatedAccount);
    account.setFirstName(updatedAccountEntity.getFirstName());
    account.setSecondName(updatedAccountEntity.getSecondName());
    account.setType(updatedAccount.getType());
    account = dao.save(account);
    return Optional.of(entityMapper.accountToInfoDto(account));
  }

  /** Метод удаления Account, по его id. */
  @Transactional
  public Boolean deleteAccount(Long id) {
    Optional<Account> account = dao.findById(id);
    if (account.isEmpty()) {
      throw new ContentNotFoundException("Account ID=" + id + " not exist");
    }
    List<AccGroupLink> links = accGroupLinkRepository.findByAccount(account.get());
    for (var link : links) {
      accGroupLinkRepository.delete(link);
    }
    if (this.getAccountEntityById(id).isPresent()) {
      dao.deleteById(id);
      return true;
    }
    return false;
  }
}
