package com.thedan17.salesnet.core.controller;

import com.thedan17.salesnet.core.dao.AccountRepository;
import com.thedan17.salesnet.core.object.dto.AccountInfoDto;
import com.thedan17.salesnet.core.object.dto.AccountSignupDto;
import com.thedan17.salesnet.core.object.dto.AccountUpdateDto;
import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.service.AccountService;
import com.thedan17.salesnet.exception.ContentNotFoundException;
import com.thedan17.salesnet.exception.ExceptionCommonLiterals;
import com.thedan17.salesnet.exception.InvalidRequestBodyException;
import com.thedan17.salesnet.util.EntityMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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

/** Контроллер для запросов, связанных с таблицей аккаунтов. */
@Tag(name = "Account API", description = "Операции, связанные напрямую с аккаунтами.")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
  @Autowired private final AccountService accountService;
  @Autowired private AccountRepository accountRepository;
  @Autowired private EntityMapper entityMapper;

  @Operation(summary = "Получить все аккаунты")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Список получен")})
  @GetMapping
  public ResponseEntity<List<AccountInfoDto>> getAllAccounts() {
    return ResponseEntity.ok(
        accountRepository.findAll().stream().map(entityMapper::accountToInfoDto).toList());
  }

  /** Конструктор для привязки соответствующего сервисного слоя. */
  public AccountController(AccountService service) {
    this.accountService = service;
  }

  /**
   * Создание нового пользователя. Делегирует вызов {@link AccountService}.
   *
   * @see AccountService#addAccount(AccountSignupDto)
   */
  @Operation(
      summary = "Создать аккаунт",
      description =
          """
        Создание нового пользователя в системе.
        - Пароль хранится в виде хэша.
        - Все поля обязательные.
        - Логин должен быть уникальным.
        """)
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Аккаунт создан"),
    @ApiResponse(
        responseCode = "400",
        description = "Передано невалидное тело запроса",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @PostMapping
  public ResponseEntity<AccountInfoDto> createAccount(
      @Valid @RequestBody AccountSignupDto account) {
    return accountService
        .addAccount(account)
        .map(infoDto -> ResponseEntity.status(HttpStatus.CREATED).body(infoDto))
        .orElseThrow(() -> new InvalidRequestBodyException("Account not created"));
  }

  /**
   * Получает пользователя по ID. Делегирует вызов {@link AccountService}.
   *
   * @see AccountService#getAccountById(Long)
   */
  @Operation(summary = "Получить аккаунт", description = "Получение аккаунта в публичном виде.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Аккаунт найден"),
    @ApiResponse(
        responseCode = "404",
        description = "Аккаунт не найден",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<AccountInfoDto> getAccountById(@Valid @Min(1) @PathVariable Long id) {
    return accountService
        .getAccountById(id)
        .map(infoDto -> ResponseEntity.status(HttpStatus.OK).body(infoDto))
        .orElseThrow(
            () -> new ContentNotFoundException(ExceptionCommonLiterals.accountNotExist(id)));
  }

  /**
   * Получение списка групп, в которых состоит аккаунт с таким ID. Делегирует вызов {@link
   * AccountService}.
   *
   * @see AccountService#getAccountGroups(Long)
   */
  @Operation(summary = "Получить группы, участником которых является аккаунт", description = "")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Возврат данных"),
    @ApiResponse(
        responseCode = "404",
        description = "Аккаунта с таким ID не существует",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @GetMapping("/{id}/groups")
  public ResponseEntity<List<GroupIdDto>> getAccountGroups(@Valid @Min(1) @PathVariable Long id) {
    return accountService
        .getAccountGroups(id)
        .map(groupIdDtoMany -> ResponseEntity.status(HttpStatus.OK).body(groupIdDtoMany))
        .orElseThrow(
            () -> new ContentNotFoundException(ExceptionCommonLiterals.accountNotExist(id)));
  }

  /**
   * Обновление аккаунта по разрешенным полям. Делегирует вызов {@link AccountService}.
   *
   * @see AccountService#updateAccount(Long, AccountUpdateDto)
   */
  @Operation(summary = "Обновить аккаунт", description = "")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Аккаунт обновлен"),
    @ApiResponse(
        responseCode = "404",
        description = "Аккаунт для обновления не найден",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
  })
  @PutMapping("/{id}")
  public ResponseEntity<AccountInfoDto> updateAccount(
      @Valid @Min(1) @PathVariable Long id, @RequestBody AccountUpdateDto updatedAccount) {
    return accountService
        .updateAccount(id, updatedAccount)
        .map(infoDto -> ResponseEntity.status(HttpStatus.OK).body(infoDto))
        .orElseThrow(
            () -> new ContentNotFoundException(ExceptionCommonLiterals.accountNotExist(id)));
  }

  /**
   * Удаление аккаунта по ID. Делегирует вызов {@link AccountService}.
   *
   * @see AccountService#deleteAccount(Long)
   */
  @Operation(summary = "Удалить аккаунт", description = "")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Аккаунт удален"),
    @ApiResponse(
        responseCode = "404",
        description = "Нечего удалять",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAccount(@Valid @Min(1) @PathVariable Long id) {
    if (Boolean.TRUE.equals(accountService.deleteAccount(id))) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      throw new ContentNotFoundException(ExceptionCommonLiterals.accountNotExist(id));
    }
  }
}
