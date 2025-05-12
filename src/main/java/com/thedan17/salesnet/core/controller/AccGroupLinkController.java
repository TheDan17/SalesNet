package com.thedan17.salesnet.core.controller;

import com.thedan17.salesnet.core.object.entity.Account;
import com.thedan17.salesnet.core.object.entity.Group;
import com.thedan17.salesnet.core.object.dto.AccGroupLinkDto;
import com.thedan17.salesnet.core.service.AccGroupLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Контроллер для создания и удаления связей между {@link Group} и {@link Account}. */
@Tag(
    name = "Links API (Account & Group) ",
    description =
        "Для операций, связанных с созданием и удалением связей между аккаунтами и группами (связь многие ко многим).")
@Controller
@RequestMapping("/api")
public class AccGroupLinkController {
  @Autowired
  AccGroupLinkService accGroupLinkService;

  /**
   * CREATE endpoint - Создание связи через точку входа {@code Account}.
   *
   * <p>Например, {@code curl -X POST accounts/2/groups?id=4}
   *
   * @param accId id аккаунта, который добавится в группу
   * @param id id группы
   * @return {@code AccGroupLink} - созданная связь в виде json
   */
  @Operation(summary = "Создание связи с группой через точку входа accounts")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Связь создана успешно"),
    @ApiResponse(
        responseCode = "400",
        description = "Один из ID не существует",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @PostMapping(value = "accounts/{accId}/groups")
  public ResponseEntity<AccGroupLinkDto> addGroupToAcc(
      @PathVariable Long accId, @RequestParam Long id) {
    return accGroupLinkService
        .linkAccWithGroup(accId, id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.badRequest().build());
  }

  /**
   * CREATE endpoint - Создание связи через точку входа {@code Group}.
   *
   * <p>Например, {@code curl -X POST groups/4/accounts?id=2}
   *
   * @param groupId id группы
   * @param id id аккаунта, который добавится в группу
   * @return объект созданной связи
   */
  @Operation(summary = "Создание связи с аккаунтом через точку входа groups")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Связь создана успешно"),
    @ApiResponse(
        responseCode = "400",
        description = "Один из ID не существует",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @PostMapping(value = "groups/{groupId}/accounts")
  public ResponseEntity<AccGroupLinkDto> addAccToGroup(
      @PathVariable Long groupId, @RequestParam Long id) {
    return accGroupLinkService
        .linkAccWithGroup(id, groupId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.badRequest().build());
  }

  /**
   * DELETE endpoint - Удаление связи через точку входа {@code Account}.
   *
   * <p>Например, {@code curl -X DELETE accounts/2/groups?id=4}
   *
   * @param accId id аккаунта, который удалится из группы
   * @param id id группы
   * @return {@code ResponseEntity}
   */
  @Operation(summary = "Удаление связи с группой через точку входа accounts")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Связь удалена успешно"),
    @ApiResponse(responseCode = "404", description = "Один из ID не существует")
  })
  @DeleteMapping(value = "accounts/{accId}/groups")
  public ResponseEntity<Void> removeGroupFromAcc(@PathVariable Long accId, @RequestParam Long id) {
    try {
      accGroupLinkService.unlinkAccWithGroup(accId, id);
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * DELETE endpoint - Удаление связи через точку входа {@code Group}.
   *
   * <p>Например, {@code curl -X DELETE groups/4/groups?id=2}
   *
   * @param groupId id группы
   * @param id id аккаунта, который удалится из группы
   * @return {@code ResponseEntity}
   */
  @Operation(summary = "Удаление связи с аккаунтом через точку входа groups")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Связь удалена успешно"),
    @ApiResponse(responseCode = "404", description = "Один из ID не существует")
  })
  @DeleteMapping(value = "groups/{groupId}/accounts")
  public ResponseEntity<Void> removeAccFromGroup(
      @PathVariable Long groupId, @RequestParam Long id) {
    try {
      accGroupLinkService.unlinkAccWithGroup(id, groupId);
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
