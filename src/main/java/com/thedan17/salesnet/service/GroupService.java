package com.thedan17.salesnet.service;

import com.thedan17.salesnet.dao.GroupRepository;
import com.thedan17.salesnet.dto.AccountInfoDto;
import com.thedan17.salesnet.dto.GroupCreateDto;
import com.thedan17.salesnet.dto.GroupIdDto;
import com.thedan17.salesnet.model.Group;
import com.thedan17.salesnet.util.CacheIdService;
import com.thedan17.salesnet.util.CommonUtil;
import com.thedan17.salesnet.util.MapperService;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Класс сервисного слоя, предлагающий операции с {@code Group}. */
@Service
public class GroupService {
  @Autowired private final GroupRepository groupDao;
  @Autowired private final MapperService mapperService;
  @Autowired private final GroupSearchCacheService groupSearchCacheService; // TODO notify about mofidying

  /** Внутренний метод для получения сущности {@code Group} напрямую. */
  @Transactional
  private Optional<Group> getGroupEntityById(Long groupId) {
    return CommonUtil.optionalFromException(
        () -> groupDao.findById(groupId).orElseThrow(), NoSuchElementException.class);
  }

  /** Конструктор с полями класса. */
  public GroupService(GroupRepository groupDao, MapperService mapperService, GroupSearchCacheService groupSearchCacheService) {
    this.groupDao = groupDao;
    this.mapperService = mapperService;
    this.groupSearchCacheService = groupSearchCacheService;
  }

  /** Метод создания нового {@code Group} и возврата информации о нём. */
  @Transactional
  public Optional<GroupIdDto> addGroup(GroupCreateDto newGroup) {
    Group result_inner;
    try {
      result_inner = groupDao.save(mapperService.createDtoToGroup(newGroup));
      groupSearchCacheService.updateExistingCache(result_inner, CacheIdService.UpdateReason.ENTITY_ADDED);
    } catch (DataIntegrityViolationException e) {
      return Optional.empty();
    }
    return Optional.of(mapperService.groupToIdDto(result_inner));
  }

  /**
   * Метод удаления уже существующего {@code Group} по id.
   *
   * @return успешность операции, определяемая отсутствием исключений
   */
  @Transactional
  public Boolean deleteGroup(Long id) {
    Optional<Group> group = groupDao.findById(id);
    if (group.isPresent()) {
      groupDao.deleteById(id);
      groupSearchCacheService.updateExistingCache(group.get(), CacheIdService.UpdateReason.ENTITY_DELETED);
    } else {
      return false;
    }
    return true;
  }

  /** Метод получения информации о существующей {@code Group}. */
  @Transactional
  public Optional<GroupIdDto> getGroupById(Long groupId) {
    return getGroupEntityById(groupId).map(mapperService::groupToIdDto);
  }

  /** Получение списка аккаунтов, которые состоят в группе, если такая существует. */
  @Transactional
  public Optional<List<AccountInfoDto>> getGroupAccounts(Long groupId) {
    Optional<Group> group =
        CommonUtil.optionalFromException(
            () -> groupDao.findById(groupId).orElseThrow(), NoSuchElementException.class);
    if (group.isEmpty()) {
      return Optional.empty();
    }
    List<AccountInfoDto> result = new ArrayList<>();
    for (var member : group.get().getMembers()) {
      result.add(mapperService.accountToInfoDto(member.getAccount()));
    }
    return Optional.of(result);
  }

  /** Поиск групп, полностью соответствующих параметру - имени группы. */
  @Transactional
  public List<GroupIdDto> searchGroups(String name) {
    Specification<Group> spec =
        ((root, query, criteriaBuilder) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (name != null) {
            predicates.add(criteriaBuilder.equal(root.get("name"), name));
          }
          return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    List<Group> groups = groupDao.findAll(spec);
    List<GroupIdDto> results = new ArrayList<>();
    for (var group : groups) {
      results.add(mapperService.groupToIdDto(group));
    }
    return results;
  }

  /** Обновление уже существующей {@code Group}. */
  @Transactional
  public Optional<GroupIdDto> updateGroup(Long id, GroupCreateDto newGroupDto) {
    Group newGroup = mapperService.createDtoToGroup(newGroupDto);
    Optional<Group> group = groupDao.findById(id);
    if (group.isEmpty()) {
      return Optional.empty();
    }
    group.get().setName(newGroup.getName());
    group.get().setDescription(newGroup.getDescription());
    groupSearchCacheService.updateExistingCache(group.get(), CacheIdService.UpdateReason.ENTITY_EDITED);
    return CommonUtil.optionalFromException(
        () -> mapperService.groupToIdDto(groupDao.save(group.get())),
        DataIntegrityViolationException.class);
  }
}
