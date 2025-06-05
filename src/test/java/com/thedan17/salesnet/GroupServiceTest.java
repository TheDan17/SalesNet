package com.thedan17.salesnet;

import com.thedan17.salesnet.core.object.entity.AccGroupLink;
import com.thedan17.salesnet.core.object.entity.Account;
import com.thedan17.salesnet.core.object.entity.Group;
import com.thedan17.salesnet.core.object.dto.AccountInfoDto;
import com.thedan17.salesnet.core.object.dto.GroupCreateDto;
import com.thedan17.salesnet.core.object.dto.GroupDto;
import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.dao.GroupRepository;
import com.thedan17.salesnet.core.service.GroupService;
import com.thedan17.salesnet.core.service.GroupSearchCacheService;
import com.thedan17.salesnet.exception.SuchElementExistException;
import com.thedan17.salesnet.util.CacheIdManager;
import com.thedan17.salesnet.util.EntityMapper;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GroupServiceTest {

  @Mock private GroupRepository groupDao;
  @Mock private EntityMapper entityMapper;
  @Mock private GroupSearchCacheService groupSearchCacheService;

  @InjectMocks private GroupService groupService;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testAddGroup_success() {
    GroupCreateDto dto = new GroupCreateDto();
    Group group = new Group();
    GroupIdDto idDto = new GroupIdDto();

    when(entityMapper.createDtoToGroup(dto)).thenReturn(group);
    when(groupDao.save(group)).thenReturn(group);
    when(entityMapper.groupToIdDto(group)).thenReturn(idDto);

    Optional<GroupIdDto> result = groupService.addGroup(dto);
    assertTrue(result.isPresent());
    assertEquals(idDto, result.get());
    verify(groupSearchCacheService).updateExistingCache(group, CacheIdManager.UpdateReason.ENTITY_ADDED);
  }

  @Test
  void testAddGroup_dataIntegrityViolation() {
    GroupCreateDto dto = new GroupCreateDto();
    Group group = new Group();

    when(entityMapper.createDtoToGroup(dto)).thenReturn(group);
    when(groupDao.save(group)).thenThrow(DataIntegrityViolationException.class);

    assertThrows(SuchElementExistException.class, ()->groupService.addGroup(dto));
  }

  @Test
  void testDeleteGroup_exists() {
    Group group = new Group();
    group.setId(1L);

    when(groupDao.findById(1L)).thenReturn(Optional.of(group));

    boolean result = groupService.deleteGroup(1L);
    assertTrue(result);
    verify(groupDao).deleteById(1L);
    verify(groupSearchCacheService).updateExistingCache(group, CacheIdManager.UpdateReason.ENTITY_DELETED);
  }

  @Test
  void testDeleteGroup_notExists() {
    when(groupDao.findById(1L)).thenReturn(Optional.empty());

    boolean result = groupService.deleteGroup(1L);
    assertFalse(result);
  }

  @Test
  void testGetGroupById_found() {
    Group group = new Group();
    GroupDto dto = new GroupDto();

    when(groupDao.findById(1L)).thenReturn(Optional.of(group));
    when(entityMapper.groupToDto(group)).thenReturn(dto);

    Optional<GroupDto> result = groupService.getGroupById(1L);
    assertTrue(result.isPresent());
    assertEquals(dto, result.get());
  }

  @Test
  void testGetGroupById_notFound() {
    when(groupDao.findById(1L)).thenReturn(Optional.empty());

    Optional<GroupDto> result = groupService.getGroupById(1L);
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetGroupAccounts_found() {
    Group group = Mockito.mock(Group.class);;
    Set<AccountInfoDto> resultSet = new HashSet<>();

    AccGroupLink member = mock(AccGroupLink.class);
    Set<AccGroupLink> members = Set.of(member);
    Account account = mock(Account.class);
    AccountInfoDto infoDto = new AccountInfoDto();

    when(groupDao.findById(1L)).thenReturn(Optional.of(group));
    when(group.getMembers()).thenReturn(members);
    when(member.getAccount()).thenReturn(account);
    when(entityMapper.accountToInfoDto(account)).thenReturn(infoDto);

    Optional<Set<AccountInfoDto>> result = groupService.getGroupAccounts(1L);
    assertTrue(result.isPresent());
    assertEquals(1, result.get().size());
  }

  @Test
  void testGetGroupAccounts_notFound() {
    when(groupDao.findById(1L)).thenReturn(Optional.empty());

    Optional<Set<AccountInfoDto>> result = groupService.getGroupAccounts(1L);
    assertTrue(result.isEmpty());
  }

  @Test
  void testSearchGroups_withName() {
    Group group = new Group();
    GroupIdDto dto = new GroupIdDto();

    when(groupDao.findAll(any(Specification.class))).thenReturn(List.of(group));
    when(entityMapper.groupToIdDto(group)).thenReturn(dto);

    List<GroupIdDto> result = groupService.searchGroups("example");
    assertEquals(1, result.size());
    assertEquals(dto, result.get(0));
  }

  @Test
  void testSearchGroups_nullName() {
    when(groupDao.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

    List<GroupIdDto> result = groupService.searchGroups(null);
    assertTrue(result.isEmpty());
  }

  @Test
  void testUpdateGroup_success() {
    GroupCreateDto dto = new GroupCreateDto();
    Group group = new Group();
    Group updated = new Group();
    GroupIdDto idDto = new GroupIdDto();

    when(entityMapper.createDtoToGroup(dto)).thenReturn(updated);
    when(groupDao.findById(1L)).thenReturn(Optional.of(group));
    when(groupDao.save(any(Group.class))).thenReturn(group);
    when(entityMapper.groupToIdDto(group)).thenReturn(idDto);

    Optional<GroupIdDto> result = groupService.updateGroup(1L, dto);
    assertTrue(result.isPresent());
    assertEquals(idDto, result.get());
  }

  @Test
  void testUpdateGroup_notFound() {
    GroupCreateDto dto = new GroupCreateDto();
    Group updated = new Group();

    when(entityMapper.createDtoToGroup(dto)).thenReturn(updated);
    when(groupDao.findById(1L)).thenReturn(Optional.empty());

    Optional<GroupIdDto> result = groupService.updateGroup(1L, dto);
    assertTrue(result.isEmpty());
  }

  @Test
  void testUpdateGroup_dataIntegrityException() {
    GroupCreateDto dto = new GroupCreateDto();
    Group existing = new Group();
    Group updated = new Group();

    when(entityMapper.createDtoToGroup(dto)).thenReturn(updated);
    when(groupDao.findById(1L)).thenReturn(Optional.of(existing));
    when(groupDao.save(any())).thenThrow(DataIntegrityViolationException.class);

    Optional<GroupIdDto> result = groupService.updateGroup(1L, dto);
    assertTrue(result.isEmpty());
  }
}

