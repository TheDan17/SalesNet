package com.thedan17.salesnet.controller;

import com.thedan17.salesnet.dto.AccountInfoDto;
import com.thedan17.salesnet.dto.GroupCreateDto;
import com.thedan17.salesnet.dto.GroupDto;
import com.thedan17.salesnet.dto.GroupIdDto;
import com.thedan17.salesnet.model.Group;
import com.thedan17.salesnet.service.GroupService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Контроллер для операций, связанных непосредственно с сущностью {@code Group}. */
@RestController
@RequestMapping("/api/groups")
public class GroupController {
  @Autowired private GroupService groupService;

  /** CREATE endpoint - Создание нового {@code Group}. */
  @PostMapping
  public ResponseEntity<?> createGroup(@RequestBody GroupCreateDto newGroup) {
    return groupService
        .addGroup(newGroup)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.internalServerError().build());
  }

  /** READ endpoint - Получение информации об уже существующем {@code Group} по его id. */
  @GetMapping("/{id}")
  public ResponseEntity<?> getGroupById(@PathVariable Long id) {
    return groupService
        .getGroupById(id)
        .map(ResponseEntity::ok) // Если Optional содержит группу
        .orElse(ResponseEntity.notFound().build()); // Если Optional пустой
  }

  /** READ endpoint - Получение списка информации об аккаунтах, состоящих в {@code Group}. */
  @GetMapping("/{id}/accounts")
  public ResponseEntity<?> getGroupAccounts(@PathVariable Long id) {
    Optional<List<AccountInfoDto>> accounts = groupService.getGroupAccounts(id);
    if (accounts.isPresent()) {
      return ResponseEntity.ok(accounts.get());
    }
    return ResponseEntity.notFound().build();
  }

  /** UPDATE endpoint - Обновление информации о группе. */
  @PutMapping("/{id}")
  public ResponseEntity<?> updateGroup(@PathVariable Long id, @RequestBody GroupCreateDto updatedGroup) {
    Optional<GroupIdDto> groupIdDto = groupService.updateGroup(id, updatedGroup);
    return groupIdDto
        .map(ResponseEntity::ok) // Если Optional содержит группу
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build()); // Если Optional пустой
  }

  /** DELETE endpoint - Удаление группы, существующей с таким id.
   *
   * <p>Существующие связи удаляться путём настроенного каскадирования
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
    if (groupService.deleteGroup(id)) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }
}
