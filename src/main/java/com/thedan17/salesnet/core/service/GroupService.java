package com.thedan17.salesnet.core.service;

import com.thedan17.salesnet.core.dao.GroupRepository;
import com.thedan17.salesnet.core.object.dto.AccountInfoDto;
import com.thedan17.salesnet.core.object.dto.GroupCreateDto;
import com.thedan17.salesnet.core.object.dto.GroupDto;
import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.object.entity.Group;
import com.thedan17.salesnet.util.CacheIdManager;
import com.thedan17.salesnet.util.CommonUtil;
import com.thedan17.salesnet.util.EntityMapper;
import jakarta.persistence.criteria.Predicate;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Класс сервисного слоя, предлагающий операции с {@code Group}. */
@Service
public class GroupService {
  @Autowired private final GroupRepository groupDao;
  @Autowired private final EntityMapper entityMapper;
  @Autowired private final GroupSearchCacheService groupSearchCacheService;

  /** Внутренний метод для получения сущности {@code Group} напрямую. */
  @Transactional
  private Optional<Group> getGroupEntityById(Long groupId) {
    return CommonUtil.optionalFromException(
        () -> groupDao.findById(groupId).orElseThrow(), NoSuchElementException.class);
  }

  /** Конструктор с полями класса. */
  public GroupService(
      GroupRepository groupDao,
      EntityMapper entityMapper,
      GroupSearchCacheService groupSearchCacheService) {
    this.groupDao = groupDao;
    this.entityMapper = entityMapper;
    this.groupSearchCacheService = groupSearchCacheService;
  }

  /** Метод создания нового {@code Group} и возврата информации о нём. */
  @Transactional
  public Optional<GroupIdDto> addGroup(GroupCreateDto newGroup) {
    Group resultInner;
    try {
      resultInner = groupDao.save(entityMapper.createDtoToGroup(newGroup));
      groupSearchCacheService.updateExistingCache(
          resultInner, CacheIdManager.UpdateReason.ENTITY_ADDED);
    } catch (DataIntegrityViolationException e) {
      return Optional.empty();
    }
    return Optional.of(entityMapper.groupToIdDto(resultInner));
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
      groupSearchCacheService.updateExistingCache(
          group.get(), CacheIdManager.UpdateReason.ENTITY_DELETED);
    } else {
      return false;
    }
    return true;
  }

  /** Метод получения информации о существующей {@code Group}. */
  @Transactional
  public Optional<GroupDto> getGroupById(Long groupId) {
    return getGroupEntityById(groupId).map(entityMapper::groupToDto);
  }

  /** Получение списка аккаунтов, которые состоят в группе, если такая существует. */
  @Transactional
  public Optional<Set<AccountInfoDto>> getGroupAccounts(Long groupId) {
    Optional<Group> group =
        CommonUtil.optionalFromException(
            () -> groupDao.findById(groupId).orElseThrow(), NoSuchElementException.class);
    if (group.isEmpty()) {
      return Optional.empty();
    }
    Set<AccountInfoDto> result = new HashSet<>();
    for (var member : group.get().getMembers()) {
      result.add(entityMapper.accountToInfoDto(member.getAccount()));
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
      results.add(entityMapper.groupToIdDto(group));
    }
    return results;
  }

  /** Обновление уже существующей {@code Group}. */
  @Transactional
  public Optional<GroupIdDto> updateGroup(Long id, GroupCreateDto newGroupDto) {
    Group newGroup = entityMapper.createDtoToGroup(newGroupDto);
    Optional<Group> group = groupDao.findById(id);
    if (group.isEmpty()) {
      return Optional.empty();
    }
    group.get().setName(newGroup.getName());
    group.get().setDescription(newGroup.getDescription());
    group.get().setOwnerId(newGroup.getOwnerId());
    groupSearchCacheService.updateExistingCache(
        group.get(), CacheIdManager.UpdateReason.ENTITY_EDITED);
    return CommonUtil.optionalFromException(
        () -> entityMapper.groupToIdDto(groupDao.save(group.get())),
        DataIntegrityViolationException.class);
  }
}
