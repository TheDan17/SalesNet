package com.thedan17.salesnet.core.controller;

import com.thedan17.salesnet.core.object.data.BulkResultDetailed;
import com.thedan17.salesnet.core.object.dto.AccountSignupDto;
import com.thedan17.salesnet.core.service.AccountBulkService;
import com.thedan17.salesnet.util.BulkResultResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для bulk операций, связанных с {@link
 * com.thedan17.salesnet.core.object.entity.Account}.
 */
@Tag(name = "Bulk API", description = "Операции пакетной обработки")
@RestController
@RequestMapping("/api")
public class AccountBulkController {
  @Autowired AccountBulkService accountBulkService;

  public AccountBulkController(AccountBulkService accountBulkService) {
    this.accountBulkService = accountBulkService;
  }

  /**
   * Массовое создание аккаунтов. Делегирует операцию {@link AccountBulkService}.
   *
   * @see AccountBulkService#addAccountsBulk(List)
   */
  @Operation(
      summary = "Создать множество аккаунтов",
      description =
          "Возвращает структуру, отображающую успешные и неуспешные по обработке элементы")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Все элементы были обработаны успешно"),
    @ApiResponse(responseCode = "207", description = "Элементы были частично успешно обработаны"),
    @ApiResponse(responseCode = "400", description = "Ни один элемент не был обработан успешно")
  })
  @PostMapping("/accounts/bulk")
  ResponseEntity<Void> addAccountsBulkWhole(@RequestBody List<AccountSignupDto> accountsData) {
    accountBulkService.addAccountsBulkWhole(accountsData);
    return ResponseEntity.ok().build();
  }

  /**
   * Массовое создание аккаунтов с частиным результатом. Делегирует операцию {@link
   * AccountBulkService}.
   *
   * @see AccountBulkService#addAccountsBulk(List)
   */
  @Operation(
      summary = "Создать множество аккаунтов",
      description =
          "Возвращает структуру, отображающую успешные и неуспешные по обработке элементы")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Все элементы были обработаны успешно"),
    @ApiResponse(responseCode = "207", description = "Элементы были частично успешно обработаны"),
    @ApiResponse(responseCode = "400", description = "Ни один элемент не был обработан успешно")
  })
  @PostMapping("/accounts/bulk-partial")
  ResponseEntity<BulkResultDetailed> addAccountsBulk(
      @RequestBody List<AccountSignupDto> accountsData) {
    BulkResultDetailed addAccountsResult = accountBulkService.addAccountsBulk(accountsData);
    return BulkResultResponseFactory.fromResult(addAccountsResult);
  }
}
