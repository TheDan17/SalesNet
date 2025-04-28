package com.thedan17.salesnet.core.controller;

import com.thedan17.salesnet.core.object.dto.AccountInfoDto;
import com.thedan17.salesnet.core.object.dto.GroupCreateDto;
import com.thedan17.salesnet.core.object.dto.GroupDto;
import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Контроллер для операций, связанных непосредственно с сущностью {@code Group}. */
@Tag(name = "Group API", description = "Операции, связанные напрямую с группами")
@RestController
@RequestMapping("/api/groups")
public class GroupController {
  @Autowired private GroupService groupService;

  /**
   * CREATE endpoint - Создание нового {@code Group}. Делегирует вызов {@link GroupService}
   *
   * @see GroupService#addGroup(GroupCreateDto)
   */
  @Operation(
      summary = "Создание группы",
      description = "Создание новой группы в системе. Все поля обязательные.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Группа создана"),
    @ApiResponse(
        responseCode = "400",
        description = "Группа не создана по требуемому телу",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @PostMapping
  public ResponseEntity<GroupIdDto> createGroup(@RequestBody GroupCreateDto newGroup) {
    return groupService
        .addGroup(newGroup)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.badRequest().build());
  }

  /**
   * READ endpoint - Получение информации об уже существующем {@code Group} по его id. Делегирует
   * вызов {@link GroupService}
   *
   * @see GroupService#getGroupById(Long)
   */
  @Operation(summary = "Получение группы по ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Группа найдена"),
    @ApiResponse(
        responseCode = "404",
        description = "Группа по такому ID не существует",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<GroupDto> getGroupById(@PathVariable Long id) {
    return groupService
        .getGroupById(id)
        .map(ResponseEntity::ok) // Если Optional содержит группу
        .orElse(ResponseEntity.notFound().build()); // Если Optional пустой
  }

  /**
   * READ endpoint - Получение списка информации об аккаунтах, состоящих в {@code Group}. Делегирует
   * вызов {@link GroupService}
   *
   * @see GroupService#getGroupAccounts(Long)
   */
  @Operation(summary = "Получение аккаунтов-участников группы по её ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Группа найдена, список возвращён"),
    @ApiResponse(
        responseCode = "404",
        description = "Группа по такому ID не существует",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @GetMapping("/{id}/accounts")
  public ResponseEntity<Set<AccountInfoDto>> getGroupAccounts(@PathVariable Long id) {
    return groupService
        .getGroupAccounts(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * UPDATE endpoint - Обновление информации о группе. Делегирует вызов {@link GroupService}
   *
   * @see GroupService#updateGroup(Long, GroupCreateDto)
   */
  @Operation(summary = "Обновление группы по ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Группа обновлена"),
    @ApiResponse(
        responseCode = "404",
        description = "Группа по такому ID не существует",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @PutMapping("/{id}")
  public ResponseEntity<GroupDto> updateGroup(
      @PathVariable Long id, @RequestBody GroupCreateDto updatedGroup) {
    return groupService
        .updateGroup(id, updatedGroup)
        .map(ResponseEntity::ok) // Если Optional содержиLт группу
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build()); // Если Optional пустой
  }

  /**
   * DELETE endpoint - Удаление группы, существующей с таким id. Делегирует вызов {@link
   * GroupService}
   *
   * <p>Существующие связи будут удаляться путём настроенного каскадирования
   *
   * @see GroupService#deleteGroup(Long)
   */
  @Operation(summary = "Удаление группы по ID")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Группа удалена"),
    @ApiResponse(
        responseCode = "404",
        description = "Группа по такому ID не существует",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
    if (Boolean.TRUE.equals(groupService.deleteGroup(id))) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }
}
