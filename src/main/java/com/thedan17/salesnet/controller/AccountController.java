package com.thedan17.salesnet.controller;

import com.thedan17.salesnet.dto.AccountInfoDto;
import com.thedan17.salesnet.dto.AccountLoginDto;
import com.thedan17.salesnet.dto.AccountUpdateDto;
import com.thedan17.salesnet.dto.GroupIdDto;
import com.thedan17.salesnet.service.AccountService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
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

/** Контроллер для запросов, связанных с таблицей аккаунтов. */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
  @Autowired private final AccountService accountService;

  /** Конструктор для привязки соответствующего сервисного слоя. */
  public AccountController(AccountService service) {
    this.accountService = service;
  }

  /** CREATE endpoint для создания нового {@code Account}. */
  @PostMapping
  public ResponseEntity<?> createAccount(@RequestBody AccountLoginDto account) {
    Optional<AccountInfoDto> accountInfoDto = accountService.addAccount(account);
    if (accountInfoDto.isEmpty()) {
      return ResponseEntity.badRequest().build();
    } else {
      return ResponseEntity.ok(accountInfoDto.get());
    }
  }

  /** READ endpoint для получения существующего {@code Account}. */
  @GetMapping("/{id}")
  public ResponseEntity<?> getAccountById(@PathVariable Long id) {
    Optional<AccountInfoDto> accountInfoDto = accountService.getAccountById(id);
    if (accountInfoDto.isEmpty()) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(accountInfoDto.get());
    }
  }

  /** READ endpoint для получения списка {@code Group}, в которых состоит {@code Account}. */
  @GetMapping("/{id}/groups")
  public ResponseEntity<?> getAccountGroups(@PathVariable Long id) {
    Optional<List<GroupIdDto>> groupDtoList = accountService.getAccountGroups(id);
    if (groupDtoList.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(groupDtoList.get());
  }

  /** READ endpoint для поиска {@code Account} по опциональным параметрам. */
  @GetMapping("/search")
  public ResponseEntity<?> searchAccounts(
      @RequestParam(required = false) String firstName,
      @RequestParam(required = false) String secondName,
      @RequestParam(required = false) String type) {
    Optional<List<AccountInfoDto>> accountInfoDtoList =
        accountService.searchAccounts(firstName, secondName, type);
    if (accountInfoDtoList.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(accountInfoDtoList.get());
  }

  /** UPDATE endpoint для обновления уже существующего {@code Account} по разрешённым полям. */
  @PutMapping("/{id}")
  public ResponseEntity<?> updateAccount(
          @PathVariable Long id, @RequestBody AccountUpdateDto updatedAccount) {
    Optional<AccountInfoDto> accountInfoDto = accountService.updateAccount(id, updatedAccount);
    if (accountInfoDto.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(accountInfoDto.get());
  }

  /** DELETE endpoint для удаления существующего {@code Account}. */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
    if (accountService.deleteAccount(id)) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
