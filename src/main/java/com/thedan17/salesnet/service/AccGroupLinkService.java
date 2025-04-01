package com.thedan17.salesnet.service;

import com.thedan17.salesnet.dao.AccGroupLinkRepository;
import com.thedan17.salesnet.dao.AccountRepository;
import com.thedan17.salesnet.dao.GroupRepository;
import com.thedan17.salesnet.dto.AccGroupLinkDto;
import com.thedan17.salesnet.model.AccGroupLink;
import com.thedan17.salesnet.model.Account;
import com.thedan17.salesnet.model.Group;
import com.thedan17.salesnet.util.MapperService;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Класс сервисного слоя, который отвечает за добавление и удаление связей {@code AccGroupLink}. */
@Service
public class AccGroupLinkService {
  @Autowired private final GroupRepository groupRepository;
  @Autowired private final AccountRepository accountRepository;
  @Autowired private final AccGroupLinkRepository accGroupLinkRepository;
  @Autowired private final MapperService mapperService;

  /** Конструктор сервисного класса для автопривязки Spring. */
  public AccGroupLinkService(
      GroupRepository groupRepository,
      AccountRepository accountRepository,
      AccGroupLinkRepository accGroupLinkRepository,
      MapperService mapperService) {
    this.groupRepository = groupRepository;
    this.accountRepository = accountRepository;
    this.accGroupLinkRepository = accGroupLinkRepository;
    this.mapperService = mapperService;
  }

  /** Добавление уникальной связи между {@code Account} и {@code Group}, GET запрос. */
  public Optional<AccGroupLinkDto> linkAccWithGroup(Long accId, Long groupId) {
    Account account = accountRepository.findById(accId).orElseThrow();
    Group group = groupRepository.findById(groupId).orElseThrow();
    if (group.getMembers().stream().anyMatch(link -> link.getAccount().equals(account))) {
      return Optional.empty();
    }
    AccGroupLink accGroupLink = new AccGroupLink();
    accGroupLink.setAccount(account);
    accGroupLink.setGroup(group);
    accGroupLink = accGroupLinkRepository.save(accGroupLink);
    // Synchronize
    account.getMembers().add(accGroupLink);
    group.getMembers().add(accGroupLink);
    return Optional.of(mapperService.linkToDto(accGroupLink));
  }

  /** Удаление существующей связи между {@code Account} и {@code Group}, DELETE запрос. */
  public void unlinkAccWithGroup(Long accId, Long groupId) {
    AccGroupLink accGroupLink =
        accGroupLinkRepository
            .findByAccountIdAndGroupId(accId, groupId)
            .orElseThrow(() -> new EntityNotFoundException("Связь не найдена"));
    accGroupLink.getAccount().getMembers().remove(accGroupLink);
    accGroupLink.getGroup().getMembers().remove(accGroupLink);
    accGroupLinkRepository.delete(accGroupLink);
  }
}
