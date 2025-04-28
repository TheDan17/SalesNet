package com.thedan17.salesnet.core.controller;

import com.thedan17.salesnet.core.service.DebugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Контроллер для отладочных действий на время разработки до релиза. */
@Tag(name = "Debug API", description = "Запросы преимущественно для разработчиков")
@RestController
@RequestMapping("/api/debug")
public class DebugController {
  @Autowired private final EntityManagerFactory entityManagerFactory;
  @Autowired private final DebugService debugService;

  public DebugController(DebugService debugService, EntityManagerFactory entityManagerFactory) {
    this.debugService = debugService;
    this.entityManagerFactory = entityManagerFactory;
  }

  /** Метод, который должен очищать кэш 2L-уровня (сохраняемый между сессиями). */
  @Operation(summary = "Очистка кэша текущей сессии сервера у JPA базы данных")
  @ApiResponse(responseCode = "200", description = "Требуемый кэш очищен")
  @PostMapping("/clear-cache")
  public ResponseEntity<Void> clearCache() {
    entityManagerFactory.getCache().evictAll();
    return ResponseEntity.ok().build();
  }

  /**
   * Поиск файла с необходимой датой. Делегирует вызов {@link DebugService}
   *
   * @see DebugService#getLogByDate(LocalDate)
   */
  @Operation(summary = "Получение лога сервера на конкретную дату в виде строки. Если произошла ошибка, строка пустая")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Лог найден и возвращен"),
    @ApiResponse(responseCode = "404", description = "Лог для такой даты не найден")
  })
  @GetMapping("/log")
  public ResponseEntity<String> getLogByData(
      @RequestParam(required = true) Short year,
      @RequestParam(required = true) Short month,
      @RequestParam(required = true) Short day) {
    Optional<String> logString = debugService.getLogByDate(LocalDate.of(year, month, day));
    return logString
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(""));
  }
}
