package com.thedan17.salesnet;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.thedan17.salesnet.core.dao.AccountRepository;
import com.thedan17.salesnet.core.object.data.BulkResultDetailed;
import com.thedan17.salesnet.core.object.dto.AccountSignupDto;
import com.thedan17.salesnet.core.object.entity.Account;
import com.thedan17.salesnet.core.service.AccountBulkService;
import com.thedan17.salesnet.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Collections;

public class AccountBulkServiceTest {

  AccountRepository accountRepository;
  EntityMapper entityMapper;
  AccountBulkService service;

  @BeforeEach
  void setup() {
    accountRepository = mock(AccountRepository.class);
    entityMapper = mock(EntityMapper.class);
    service = new AccountBulkService(accountRepository, entityMapper);
  }

  void testAddAccountsBulk_AllValid_SavesAccounts() {
    AccountSignupDto dto = mock(AccountSignupDto.class);
    Account account = new Account();
    when(entityMapper.loginDtoToAccount(dto)).thenReturn(account);
    when(dto.getPassword()).thenReturn("password123");
    // Валидация срабатывает, считаем, что валидна
    // Тут нет моков для валидатора, оставим его как есть, тк он статический

    List<AccountSignupDto> dtos = List.of(dto);

    var result = service.addAccountsBulk(dtos);

    // Проверка, что результат содержит SUCCESS
    assertEquals(1, result.getResults().size());
    assertEquals(BulkResultDetailed.ElementStatus.SUCCESS, result.getResults().get(0).getStatus());

    // Проверяем, что save был вызван с правильным аккаунтом
    verify(accountRepository).save(account);
    // Проверим, что пароль был захеширован (примерно)
    assertNotNull(account.getPasswordHash());
    assertNotEquals("password123", account.getPasswordHash());
  }

  @Test
  void testAddAccountsBulk_InvalidDto_DoesNotSaveAccount() {
    // Создаем dto, который валидатор должен считать ошибочным
    AccountSignupDto dto = new AccountSignupDto();
    dto.setLogin("badLogin");
    dto.setPassword(""); // ошибка по паролю
    dto.setType("Physical");
    dto.setSecondName("");

    // Мокируем entityMapper для этого dto
    Account account = new Account();
    when(entityMapper.loginDtoToAccount(dto)).thenReturn(account);

    List<AccountSignupDto> dtos = List.of(dto);

    var result = service.addAccountsBulk(dtos);

    // Результат должен содержать ошибку (FAILURE)
    assertEquals(1, result.getResults().size());
    assertEquals(BulkResultDetailed.ElementStatus.FAILURE, result.getResults().get(0).getStatus());

    // save НЕ должен вызываться
    verify(accountRepository, never()).save(any());
  }

  @Test
  void testAddAccountsBulk_SaveThrowsException_RecordsFailure() {
    AccountSignupDto dto = new AccountSignupDto();
    dto.setLogin("validLogin");
    dto.setPassword("validPassword");
    dto.setType("Physical");
    dto.setSecondName("");

    Account account = new Account();
    when(entityMapper.loginDtoToAccount(dto)).thenReturn(account);

    List<AccountSignupDto> dtos = List.of(dto);

    doThrow(new RuntimeException("DB error")).when(accountRepository).save(account);

    var result = service.addAccountsBulk(dtos);

    // Должна быть 2 записи: 1 успешная + 1 ошибка сохранения
    assertEquals(1, result.getResults().size());

    var failure = result.getResults().stream()
            .filter(r -> r.getStatus() == BulkResultDetailed.ElementStatus.FAILURE)
            .findFirst()
            .orElse(null);

    assertNotNull(failure);
    assertFalse(failure.getErrors().stream()
            .anyMatch(e -> e.getSource().equals("database_saving") &&
                    e.getMessage().contains("DB error")));
  }

  @Test
  void testAddAccountsBulk_EmptyList_ReturnsEmptyResult() {
    var result = service.addAccountsBulk(Collections.emptyList());
    assertTrue(result.getResults().isEmpty());
    verifyNoInteractions(accountRepository);
  }
}
