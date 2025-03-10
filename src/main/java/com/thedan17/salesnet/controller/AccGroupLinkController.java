package com.thedan17.salesnet.controller;

import com.thedan17.salesnet.service.AccGroupLinkService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Контроллер для создания и удаления связей между {@code Group} и {@code Account}.
 */
@Controller
@RequestMapping("/api")
public class AccGroupLinkController {
  @Autowired AccGroupLinkService accGroupLinkService;

  /**
   * CREATE endpoint - Создание связи через точку входа {@code Account}.
   *
   * <p>Например, {@code curl -X POST accounts/2/groups?id=4}
   *
   * @param accId id аккаунта, который добавится в группу
   * @param id id группы
   * @return {@code AccGroupLink} - созданная связь в виде json
   */
  @PostMapping(value = "accounts/{accId}/groups")
  public ResponseEntity<?> addGroupToAcc(@PathVariable Long accId, @RequestParam Long id) {
    return accGroupLinkService.linkAccWithGroup(accId, id)
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
  @PostMapping(value = "groups/{groupId}/accounts")
  public ResponseEntity<?> addAccToGroup(@PathVariable Long groupId, @RequestParam Long id) {
    return accGroupLinkService.linkAccWithGroup(id, groupId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.badRequest().build());
  }

  /**
   * DELETE endpoint - Удаление связи через точку входа {@code Account}.
   *
   * <p>Например, {@code curl -X DELETE accounts/2/groups?id=4}
   *
   * <p>Возможные ответы:
   * <ul>
   *   <li>{@code 204 No Content} — в случае успешного удаления</li>
   *   <li>{@code 404 Not Found} — если связь не найдена</li>
   * </ul>
   *
   * @param accId id аккаунта, который удалится из группы
   * @param id id группы
   * @return {@code ResponseEntity}
   */
  @DeleteMapping(value = "accounts/{accId}/groups")
  public ResponseEntity<?> removeGroupFromAcc(@PathVariable Long accId, @RequestParam Long id) {
    try {
      accGroupLinkService.unlinkAccWithGroup(accId, id);
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * DELETE endpoint - Удаление связи через точку входа {@code Group}.
   *
   * <p>Например, {@code curl -X DELETE groups/4/groups?id=2}
   *
   * <p>Возможные ответы:
   * <ul>
   *   <li>{@code 204 No Content} — в случае успешного удаления</li>
   *   <li>{@code 404 Not Found} — если связь не найдена</li>
   * </ul>
   *
   * @param groupId id группы
   * @param id id аккаунта, который удалится из группы
   * @return {@code ResponseEntity}
   */
  @DeleteMapping(value = "groups/{groupId}/accounts")
  public ResponseEntity<?> removeAccFromGroup(@PathVariable Long groupId, @RequestParam Long id) {
    try {
      accGroupLinkService.unlinkAccWithGroup(id, groupId);
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
