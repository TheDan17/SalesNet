package com.thedan17.salesnet.core.service;

import com.thedan17.salesnet.core.dao.AccountRepository;
import com.thedan17.salesnet.core.dao.GroupRepository;
import com.thedan17.salesnet.core.object.dto.AccGroupLinkCreateDto;
import com.thedan17.salesnet.core.object.dto.AccGroupLinkDto;
import com.thedan17.salesnet.core.object.entity.AccGroupLink;
import com.thedan17.salesnet.core.object.entity.Account;
import com.thedan17.salesnet.core.object.entity.Group;
import com.thedan17.salesnet.exception.ContentNotFoundException;
import com.thedan17.salesnet.util.EntityMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Класс сервисного слоя, который отвечает за добавление и удаление связей {@code AccGroupLink}. */
@Service
public class AccGroupLinkService {
  @Autowired private final GroupRepository groupRepository;
  @Autowired private final AccountRepository accountRepository;
  @Autowired private final com.thedan17.salesnet.core.dao.AccGroupLinkRepository accGroupLinkRepository;
  @Autowired private final EntityMapper entityMapper;

  /** Конструктор сервисного класса для автопривязки Spring. */
  public AccGroupLinkService(
      GroupRepository groupRepository,
      AccountRepository accountRepository,
      com.thedan17.salesnet.core.dao.AccGroupLinkRepository accGroupLinkRepository,
      EntityMapper entityMapper) {
    this.groupRepository = groupRepository;
    this.accountRepository = accountRepository;
    this.accGroupLinkRepository = accGroupLinkRepository;
    this.entityMapper = entityMapper;
  }

  /** Добавление уникальной связи между {@code Account} и {@code Group}, GET запрос. */
  @Transactional
  public Optional<AccGroupLinkDto> linkAccWithGroup(AccGroupLinkCreateDto dto) {
    Optional<Account> accountOptional = accountRepository.findById(dto.getAccountId());
    Optional<Group> groupOptional = groupRepository.findById(dto.getGroupId());
    if (groupOptional.isEmpty()) {
      throw new ContentNotFoundException("Group with such ID not found");
    }
    if (accountOptional.isEmpty()){
      throw new ContentNotFoundException("Account with such ID not found");
    }
    Group group = groupOptional.get();
    Account account = accountOptional.get();

    if (group.getMembers().stream().anyMatch(link -> link.getAccount().equals(account))) {
      return Optional.empty();
    }
    AccGroupLink accGroupLink = entityMapper.createDtoToLink(dto);
    accGroupLink.setAccount(account);
    accGroupLink.setGroup(group);
    accGroupLink = accGroupLinkRepository.save(accGroupLink);
    // Synchronize
    group.getMembers().add(accGroupLink);
    return Optional.of(entityMapper.linkToDto(accGroupLink));
  }

  @Transactional
  public void deleteLink(Long linkId) {
    AccGroupLink accGroupLink =
        accGroupLinkRepository
            .findById(linkId)
            .orElseThrow(() -> new ContentNotFoundException("Link (group-account) ID=" + linkId + " not found"));
    accGroupLink.getGroup().getMembers().remove(accGroupLink);
    accGroupLinkRepository.delete(accGroupLink);
  }

  /** Удаление существующей связи между {@code Account} и {@code Group}, DELETE запрос. */
  @Transactional
  public void unlinkAccWithGroup(Long accId, Long groupId) {
    AccGroupLink accGroupLink =
        accGroupLinkRepository
            .findByAccountIdAndGroupId(accId, groupId)
            .orElseThrow(() -> new EntityNotFoundException("Связь не найдена"));
    accGroupLink.getGroup().getMembers().remove(accGroupLink);
    accGroupLinkRepository.delete(accGroupLink);
  }
}
