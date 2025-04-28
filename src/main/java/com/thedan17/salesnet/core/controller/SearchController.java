package com.thedan17.salesnet.core.controller;

import com.thedan17.salesnet.core.dao.AccountRepository;
import com.thedan17.salesnet.core.object.dto.AccountInfoDto;
import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.service.AccountService;
import com.thedan17.salesnet.core.service.GroupSearchCacheService;
import com.thedan17.salesnet.exception.InvalidSearchParameterException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Контроллер специально для кэшируемого поиска групп по параметрам. */
@Tag(name = "Search API", description = "Операции поиска")
@RestController
@RequestMapping("/api")
public class SearchController {
  @Autowired GroupSearchCacheService groupSearchCacheService;
  @Autowired AccountService accountService;
  @Autowired AccountRepository accountRepository;

  /**
   * Поиск аккаунтов по необязательным критериям. Делегирует вызов {@link AccountService}.
   *
   * @see AccountService#searchAccounts(String, String, String)
   */
  @Operation(summary = "Поиск аккаунтов по критериям", description = "")
  @ApiResponse(responseCode = "200", description = "Поиск завершен успешно")
  @GetMapping("accounts/search")
  public ResponseEntity<List<AccountInfoDto>> searchAccounts(
          @RequestParam(required = false) String firstName,
          @RequestParam(required = false) String secondName,
          @RequestParam(required = false) String type) {
    return ResponseEntity.status(HttpStatus.OK)
            .body(accountService.searchAccounts(firstName, secondName, type));
  }

  /** Поиск групп. */
  @GetMapping("groups/search")
  @Operation(
      summary =
          "Выполнение поиска в двух вариантах: только по имени, и дополнительно по аккаунту, среди групп, в которых он состоит. Также для всех вариантов запросы кэшируются")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Поиск выполнен успешно"),
    @ApiResponse(
        responseCode = "404",
        description = "ID аккаунта указан, но такой аккаунт не существует",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "503",
        description = "Произошла внутренняя ошибка во время поиска",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  public ResponseEntity<Set<GroupIdDto>> searchGroups(
      @RequestParam String name, @RequestParam(required = false) Long accId) {
    if (name.length() <= 3) {
      throw new InvalidSearchParameterException(
          "To search groups by name, length of value must be more than 3");
    }
    if (accId != null && !accountRepository.existsById(accId)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new HashSet<>()); // "Account with such id not exist in database"
    }
    return groupSearchCacheService
        .searchGroups(name, accId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
  }
}
