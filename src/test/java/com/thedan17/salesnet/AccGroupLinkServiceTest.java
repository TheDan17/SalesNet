package com.thedan17.salesnet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.thedan17.salesnet.core.dao.*;
import com.thedan17.salesnet.core.object.dto.*;
import com.thedan17.salesnet.core.object.entity.*;
import com.thedan17.salesnet.core.service.AccGroupLinkService;
import com.thedan17.salesnet.exception.ContentNotFoundException;
import com.thedan17.salesnet.util.EntityMapper;

import jakarta.persistence.EntityNotFoundException;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccGroupLinkServiceTest {

  private GroupRepository groupRepository;
  private AccountRepository accountRepository;
  private AccGroupLinkRepository accGroupLinkRepository;
  private EntityMapper entityMapper;
  private AccGroupLinkService service;

  @BeforeEach
  void setup() {
    groupRepository = mock(GroupRepository.class);
    accountRepository = mock(AccountRepository.class);
    accGroupLinkRepository = mock(AccGroupLinkRepository.class);
    entityMapper = mock(EntityMapper.class);

    service = new AccGroupLinkService(groupRepository, accountRepository, accGroupLinkRepository, entityMapper);
  }

  @Test
  void testLinkAccWithGroup_success() {
    var dto = new AccGroupLinkCreateDto(1L, 2L, "");

    var account = new Account();
    account.setId(1L);

    var group = new Group();
    group.setId(2L);
    group.setMembers(new HashSet<>());

    var accGroupLink = new AccGroupLink();
    var returnedDto = new AccGroupLinkDto();

    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
    when(groupRepository.findById(2L)).thenReturn(Optional.of(group));
    when(entityMapper.createDtoToLink(dto)).thenReturn(accGroupLink);
    when(accGroupLinkRepository.save(accGroupLink)).thenReturn(accGroupLink);
    when(entityMapper.linkToDto(accGroupLink)).thenReturn(returnedDto);

    var result = service.linkAccWithGroup(dto);

    assertTrue(result.isPresent());
    assertEquals(returnedDto, result.get());

    verify(accGroupLinkRepository).save(accGroupLink);
    assertTrue(group.getMembers().contains(accGroupLink));
  }

  @Test
  void testLinkAccWithGroup_groupNotFound() {
    var dto = new AccGroupLinkCreateDto(1L, 2L, "");
    when(groupRepository.findById(2L)).thenReturn(Optional.empty());

    ContentNotFoundException ex = assertThrows(ContentNotFoundException.class, () -> {
      service.linkAccWithGroup(dto);
    });
    assertEquals("Group with such ID not found", ex.getMessage());
  }

  @Test
  void testLinkAccWithGroup_accountNotFound() {
    var dto = new AccGroupLinkCreateDto(1L, 2L, "");
    var group = new Group();
    group.setMembers(new HashSet<>());
    when(groupRepository.findById(2L)).thenReturn(Optional.of(group));
    when(accountRepository.findById(1L)).thenReturn(Optional.empty());

    ContentNotFoundException ex = assertThrows(ContentNotFoundException.class, () -> {
      service.linkAccWithGroup(dto);
    });
    assertEquals("Account with such ID not found", ex.getMessage());
  }

  @Test
  void testLinkAccWithGroup_alreadyLinked_returnsEmpty() {
    var dto = new AccGroupLinkCreateDto(1L, 2L, "");
    var account = new Account(); account.setId(1L);
    var group = new Group(); group.setMembers(new HashSet<>());

    var existingLink = new AccGroupLink();
    existingLink.setAccount(account);
    group.getMembers().add(existingLink);

    when(groupRepository.findById(2L)).thenReturn(Optional.of(group));
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

    var result = service.linkAccWithGroup(dto);
    assertTrue(result.isEmpty());

    verify(accGroupLinkRepository, never()).save(any());
  }

  @Test
  void testDeleteLink_success() {
    var link = new AccGroupLink();
    var group = new Group();
    group.setMembers(new HashSet<>(List.of(link)));
    link.setGroup(group);

    when(accGroupLinkRepository.findById(10L)).thenReturn(Optional.of(link));

    service.deleteLink(10L);

    assertFalse(group.getMembers().contains(link));
    verify(accGroupLinkRepository).delete(link);
  }

  @Test
  void testDeleteLink_notFound() {
    when(accGroupLinkRepository.findById(10L)).thenReturn(Optional.empty());

    var ex = assertThrows(ContentNotFoundException.class, () -> service.deleteLink(10L));
    assertTrue(ex.getMessage().contains("Link (group-account) ID=10 not found"));
  }

  @Test
  void testUnlinkAccWithGroup_success() {
    var link = new AccGroupLink();
    var group = new Group();
    group.setMembers(new HashSet<>(List.of(link)));
    link.setGroup(group);

    when(accGroupLinkRepository.findByAccountIdAndGroupId(1L, 2L)).thenReturn(Optional.of(link));

    service.unlinkAccWithGroup(1L, 2L);

    assertFalse(group.getMembers().contains(link));
    verify(accGroupLinkRepository).delete(link);
  }

  @Test
  void testUnlinkAccWithGroup_notFound() {
    when(accGroupLinkRepository.findByAccountIdAndGroupId(1L, 2L)).thenReturn(Optional.empty());

    var ex = assertThrows(EntityNotFoundException.class, () -> service.unlinkAccWithGroup(1L, 2L));
    assertEquals("Связь не найдена", ex.getMessage());
  }
}