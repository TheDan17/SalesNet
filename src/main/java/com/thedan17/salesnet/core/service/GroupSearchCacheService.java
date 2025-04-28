package com.thedan17.salesnet.core.service;

import com.thedan17.salesnet.core.dao.AccGroupLinkRepository;
import com.thedan17.salesnet.core.dao.GroupSearchRepository;
import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.object.entity.Group;
import com.thedan17.salesnet.util.CacheIdManager;
import com.thedan17.salesnet.util.CommonUtil;
import com.thedan17.salesnet.util.EntityMapper;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Сервис с настроенным кэшированным поиском групп по различным параметрам. */
@Service
public class GroupSearchCacheService {
  private final CacheIdManager<String, Group, Long> byNameFromAllCache;
  private final CacheIdManager<Pair<String, Long>, Group, Long> byNameFromAccCache;
  @Autowired private final GroupSearchRepository groupSearchRepository;
  @Autowired private final AccGroupLinkRepository accGroupLinkRepository;
  @Autowired private final EntityMapper entityMapper;

  /** Настройка функциональных частей кэша. */
  private void setCacheFunctionality() {
    // all cache
    Function<String, Set<Group>> searchAllFunction = groupSearchRepository::findByNameSubstringJpql;
    BiPredicate<String, Group> isValidAllFunction =
        (arg, res) -> res.getName().contains(arg);
    byNameFromAllCache.setFunctionality(searchAllFunction, isValidAllFunction);
    // acc cache
    Function<Pair<String, Long>, Set<Group>> searchAccFunction =
        arg -> groupSearchRepository.findByNameInAccJpql(arg.getFirst(), arg.getSecond());
    BiPredicate<Pair<String, Long>, Group> isValidAccFunction =
        (pair, res) -> {
          if (isValidAllFunction.test(pair.getFirst(), res)) {
            return accGroupLinkRepository
                .findByAccountIdAndGroupId(pair.getSecond(), res.getId())
                .isPresent();
          }
          return false;
        };
    byNameFromAccCache.setFunctionality(searchAccFunction, isValidAccFunction);
  }

  /** Конструктор класса. */
  public GroupSearchCacheService(
      GroupSearchRepository groupSearchRepository,
      AccGroupLinkRepository accGroupLinkRepository,
      EntityMapper entityMapper) {
    this.groupSearchRepository = groupSearchRepository;
    this.accGroupLinkRepository = accGroupLinkRepository;
    this.entityMapper = entityMapper;
    byNameFromAllCache = new CacheIdManager<>(Group::getId, 500L, (short) 10);
    byNameFromAccCache = new CacheIdManager<>(Group::getId, 500L, (short) 10);
    setCacheFunctionality();
  }

  /**
   * Поиск групп по двум параметрам, один из которых опционален.
   *
   * @param name частичное имя группы, обязательный параметр, иначе {@code Optional.empty()}
   * @param accId поиск среди групп, в которых состоит этот аккаунт
   */
  @Transactional
  public Optional<Set<GroupIdDto>> searchGroups(String name, Long accId) {
    Optional<Set<Group>> results;
    if (name != null && accId != null) {
      results =
          CommonUtil.optionalFromException(
              () -> byNameFromAccCache.doAction(Pair.of(name.toLowerCase(), accId)),
              Exception.class);
    } else if (name != null) {
      results =
          CommonUtil.optionalFromException(
              () -> byNameFromAllCache.doAction(name.toLowerCase()), Exception.class);
    } else {
      results = Optional.empty();
    }
    if (results.isEmpty()) {
      return Optional.empty();
    }
    Set<GroupIdDto> resultsDto = new HashSet<>();
    for (Group group : results.get()) {
      resultsDto.add(entityMapper.groupToIdDto(group));
    }
    return Optional.of(resultsDto);
  }

  /** Метод обновления кэшей по принципу {@link CacheIdManager#updateCache}. */
  public void updateExistingCache(Group group, CacheIdManager.UpdateReason updateReason) {
    byNameFromAllCache.updateCache(group, updateReason, true);
    byNameFromAccCache.updateCache(group, updateReason, true);
  }
}
