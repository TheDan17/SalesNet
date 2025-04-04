package com.thedan17.salesnet.service;

import com.thedan17.salesnet.dao.AccGroupLinkRepository;
import com.thedan17.salesnet.dao.GroupSearchRepository;
import com.thedan17.salesnet.dto.GroupIdDto;
import com.thedan17.salesnet.model.Group;
import com.thedan17.salesnet.util.CacheIdService;
import com.thedan17.salesnet.util.CommonUtil;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.thedan17.salesnet.util.MapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Сервис с настроенным кэшированным поиском групп по различным параметрам. */
@Service
public class GroupSearchCacheService {
  private final CacheIdService<String, Group, Long> byNameFromAllCache;
  private final CacheIdService<Pair<String, Long>, Group, Long> byNameFromAccCache;
  @Autowired private final GroupSearchRepository groupSearchRepository;
  @Autowired private final AccGroupLinkRepository accGroupLinkRepository;
  @Autowired private final MapperService mapperService;

  /** Настройка функциональных частей кэша. */
  private void setCacheFunctionality() {
    // all cache
    Function<String, Set<Group>> searchAllFunction =
        (arg) -> groupSearchRepository.findByNameSubstringJpql(arg);
    BiFunction<String, Group, Boolean> isValidAllFunction =
        (arg, res) -> res.getName().contains(arg);
    byNameFromAllCache.setFunctionality(searchAllFunction, isValidAllFunction);
    // acc cache
    Function<Pair<String, Long>, Set<Group>> searchAccFunction =
        (arg) -> groupSearchRepository.findByNameInAccJpql(arg.getFirst(), arg.getSecond());
    BiFunction<Pair<String, Long>, Group, Boolean> isValidAccFunction =
        (pair, res) -> {
          if (isValidAllFunction.apply(pair.getFirst(), res)) {
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
      MapperService mapperService) {
    this.groupSearchRepository = groupSearchRepository;
    this.accGroupLinkRepository = accGroupLinkRepository;
    this.mapperService = mapperService;
    byNameFromAllCache = new CacheIdService<>(Group::getId, 500L, (short)10);
    byNameFromAccCache = new CacheIdService<>(Group::getId, 500L, (short)10);
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
      resultsDto.add(mapperService.groupToIdDto(group));
    }
    return Optional.of(resultsDto);
  }

  public void updateExistingCache(Group group, CacheIdService.UpdateReason updateReason) {
    byNameFromAllCache.updateCache(group, updateReason, true);
    byNameFromAccCache.updateCache(group, updateReason, true);
  }
}
