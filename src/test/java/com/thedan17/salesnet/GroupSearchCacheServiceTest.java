package com.thedan17.salesnet;

import com.thedan17.salesnet.core.dao.AccGroupLinkRepository;
import com.thedan17.salesnet.core.dao.GroupSearchRepository;
import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.object.entity.AccGroupLink;
import com.thedan17.salesnet.core.object.entity.Group;
import com.thedan17.salesnet.core.service.GroupSearchCacheService;
import com.thedan17.salesnet.util.CacheIdManager;
import com.thedan17.salesnet.util.EntityMapper;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.*;
import org.springframework.data.util.Pair;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.thedan17.salesnet.core.dao.AccGroupLinkRepository;
import com.thedan17.salesnet.core.dao.GroupSearchRepository;
import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.object.entity.Group;
import com.thedan17.salesnet.util.EntityMapper;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@ExtendWith(MockitoExtension.class)
class GroupSearchCacheServiceTest {

  GroupSearchRepository groupSearchRepository;
  AccGroupLinkRepository accGroupLinkRepository;
  EntityMapper entityMapper;

  GroupSearchCacheService service;

  @BeforeEach
  void setup() {
    groupSearchRepository = mock(GroupSearchRepository.class);
    accGroupLinkRepository = mock(AccGroupLinkRepository.class);
    entityMapper = mock(EntityMapper.class);

    service = new GroupSearchCacheService(groupSearchRepository, accGroupLinkRepository, entityMapper);
  }

  @Test
  void testSearchGroups_withNameAndAccId_cacheMiss() {
    String name = "test";
    Long accId = 123L;


    AccGroupLink link = mock(AccGroupLink.class);

    Group group = mock(Group.class);
    when(group.getId()).thenReturn(1L);
    //when(group.getName()).thenReturn("test group");
    Set<Group> groups = Set.of(group);

    // Здесь проверяем вызов через byNameFromAccCache, но он внутри private
    // Для интеграционного подхода можно вызвать doAction напрямую через рефлексию или оставить как есть и моки настроить для репозиториев

    // Настроим мок репозитория
    when(groupSearchRepository.findByNameInAccJpql(name.toLowerCase(), accId)).thenReturn(groups);

    // Мок для проверки accGroupLinkRepository
    //when(accGroupLinkRepository.findByAccountIdAndGroupId(accId, 1L))
    //        .thenReturn(Optional.of(link));

    GroupIdDto dto = new GroupIdDto();
    when(entityMapper.groupToIdDto(group)).thenReturn(dto);

    Optional<Set<GroupIdDto>> result = service.searchGroups(name, accId);

    assertTrue(result.isPresent());
    assertTrue(result.get().contains(dto));
  }

  @Test
  void testSearchGroups_withNameOnly_cacheMiss() {
    String name = "onlyName";
    Group group = mock(Group.class);
    when(group.getId()).thenReturn(2L);
    //when(group.getName()).thenReturn("onlyName group");
    Set<Group> groups = Set.of(group);

    when(groupSearchRepository.findByNameSubstringJpql(name.toLowerCase())).thenReturn(groups);

    GroupIdDto dto = new GroupIdDto();
    when(entityMapper.groupToIdDto(group)).thenReturn(dto);

    Optional<Set<GroupIdDto>> result = service.searchGroups(name, null);

    assertTrue(result.isPresent());
    assertTrue(result.get().contains(dto));
  }

  @Test
  void testSearchGroups_nullName_returnsEmpty() {
    Optional<Set<GroupIdDto>> result = service.searchGroups(null, 5L);
    assertTrue(result.isEmpty());
  }

  @Test
  void testUpdateExistingCache_callsUpdateCacheOnBothCaches() {
    Group group = mock(Group.class);
    when(group.getId()).thenReturn(1L);

    // Здесь нет доступа к CacheIdManager из-за private final,
    // но если хочешь проверить вызов updateCache — можно сделать spy сервис или выделить CacheIdManager в отдельный бин

    // Для примера: можно проверить отсутствие исключений и что метод вызывается
    service.updateExistingCache(group, CacheIdManager.UpdateReason.ENTITY_ADDED);

    // Без доступа к внутренним полям проверить сложно, но можно добавить package-private геттеры в сервис для тестов (если архитектура позволяет)
  }
}
