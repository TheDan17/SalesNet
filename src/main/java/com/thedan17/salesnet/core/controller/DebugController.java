package com.thedan17.salesnet.core.controller;

import com.thedan17.salesnet.core.service.DebugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
  public ResponseEntity<Resource> getLogByData(
      @Valid @Min(1970) @RequestParam(required = true) Short year,
      @Valid @Min(1) @Max(12) @RequestParam(required = true) Short month,
      @Valid @Min(1) @Max(31) @RequestParam(required = true) Short day) {
    LocalDate taskParams;
    try {
      taskParams = LocalDate.of(year, month, day);
    } catch (DateTimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    Path filePath = debugService.getLogByDate(taskParams);
    if (filePath == null) {
      return ResponseEntity.notFound().build();
    }
    Resource resource = new FileSystemResource(filePath.toFile());
    String headerValue = "attachment; filename=%s".formatted(filePath.getFileName());
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
            .contentType(MediaType.TEXT_PLAIN)
            .body(resource);
  }
}
