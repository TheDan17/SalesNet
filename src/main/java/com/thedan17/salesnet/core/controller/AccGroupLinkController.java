package com.thedan17.salesnet.core.controller;

import com.thedan17.salesnet.core.dao.AccGroupLinkRepository;
import com.thedan17.salesnet.core.dao.GroupRepository;
import com.thedan17.salesnet.core.object.dto.*;
import com.thedan17.salesnet.core.object.entity.AccGroupLink;
import com.thedan17.salesnet.core.object.entity.Account;
import com.thedan17.salesnet.core.object.entity.Group;
import com.thedan17.salesnet.core.service.AccGroupLinkService;
import com.thedan17.salesnet.exception.InvalidRequestBodyException;
import com.thedan17.salesnet.util.EntityMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

  @Autowired
  GroupRepository groupRepository;
  @Autowired
  AccGroupLinkRepository accGroupLinkRepository;
  @Autowired
  EntityMapper entityMapper;

  @GetMapping("/links")
  public ResponseEntity<List<AccGroupLinkDto>> getAllLinks() {
    return ResponseEntity.ok(
            accGroupLinkRepository
                    .findAll()
                    .stream()
                    .map(entityMapper::linkToDto)
                    .toList()
    );
  }

  @PostMapping("/links")
  public ResponseEntity<AccGroupLinkDto> addLink(@Valid @RequestBody AccGroupLinkCreateDto newLink) {
    var link = accGroupLinkService.linkAccWithGroup(newLink);
    if (link.isEmpty()) {
      throw new InvalidRequestBodyException("Non-valid ID in body");
    }
    return ResponseEntity.ok(link.get());
  }

  @DeleteMapping("/links/{id}")
  public ResponseEntity<Void> deleteLink(@Valid @Min(1) @PathVariable Long id) {
    accGroupLinkService.deleteLink(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping("/links/allgroupswithaccounts")
  public ResponseEntity<List<GroupAccountsDto>> getAllGroupsAccounts() {
    List<Group> groups = groupRepository.findAll();
    List<GroupAccountsDto> dtos = new ArrayList<>();
    for (Group group : groups) {
      GroupAccountsDto dto = entityMapper.groupToGroupAccounts(group);
      List<AccGroupLink> groupAccs = accGroupLinkRepository.findByGroup(group);
      for (AccGroupLink link : groupAccs) {
        dto.getAccounts().add(entityMapper.accountToInfoDto(link.getAccount()));
      }
      dtos.add(dto);
    }
    return ResponseEntity.ok(dtos);
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
  public ResponseEntity<Void> removeGroupFromAcc(
          @Valid @Min(1) @PathVariable Long accId, @Valid @Min(1) @RequestParam Long id) {
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
          @Valid @Min(1) @PathVariable Long groupId, @Valid @Min(1) @RequestParam Long id) {
    try {
      accGroupLinkService.unlinkAccWithGroup(id, groupId);
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
