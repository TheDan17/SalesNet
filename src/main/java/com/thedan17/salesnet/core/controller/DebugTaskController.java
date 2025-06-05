package com.thedan17.salesnet.core.controller;

import com.thedan17.salesnet.core.object.data.AsyncTaskInfo;
import com.thedan17.salesnet.core.service.DebugTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Debug Task API", description = "Запросы для разработчиков, требующие обработки")
@RestController
@RequestMapping("/api/debug-async")
public class DebugTaskController {
  @Autowired
  private final DebugTaskService debugTaskService;

  public DebugTaskController(DebugTaskService debugTaskService) {
    this.debugTaskService = debugTaskService;
  }

  @Operation(summary = "Создание задачи получения лог-файла по дате")
  @ApiResponses({
          @ApiResponse(responseCode = "201", description = "Задача создана"),
          @ApiResponse(responseCode = "400", description = "Дата невалидна")
  })
  @PostMapping("/log/create_task")
  public ResponseEntity<Integer> createGetLogTask(@Valid @Min(1970) @RequestParam Short year,
                                                  @Valid @Min(1) @Max(12) @RequestParam Short month,
                                                  @Valid @Min(1) @Max(31)  @RequestParam Short day) {
    LocalDate taskParams;
    try {
      taskParams = LocalDate.of(year, month, day);
    } catch (DateTimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(-1);
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(debugTaskService.getLogNewTask(taskParams));
  }

  @Operation(summary = "Получение списка информации по всем существующим задачам")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Список задач получен")
  })
  @GetMapping("/log/get_all_tasks")
  public ResponseEntity<List<AsyncTaskInfo<LocalDate, Path>>> getAllTasks() {
    return ResponseEntity.ok(debugTaskService.getAllTasks());
  }

  @Operation(summary = "Получение статуса конкретной задачи")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Статус получен"),
          @ApiResponse(responseCode = "404", description = "Задачи с таким ID не существует")
  })
  @GetMapping("/log/check_task")
  public ResponseEntity<AsyncTaskInfo.Status> getStatus(@Valid @Min(1) @RequestParam Integer taskId) {
    return ResponseEntity.ok(debugTaskService.getTaskStatus(taskId));
  }

  @Operation(summary = "Получение результата выполнения задачи")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Файл получен"),
          @ApiResponse(responseCode = "202", description = "Задача существует, но не завершена"),
          @ApiResponse(responseCode = "404", description = "Задача с таким ID не существует")
  })
  @GetMapping("/log/get_result")
  public ResponseEntity<Resource> downloadFile(@Valid @Min(1) @RequestParam Integer taskId) {
    Path filePath = debugTaskService.getLogFile(taskId);
    if (filePath == null) {
      return ResponseEntity.noContent().build();
    }
    Resource resource = new FileSystemResource(filePath.toFile());
    String headerValue = "attachment; filename=%s".formatted(filePath.getFileName());
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
            .contentType(MediaType.TEXT_PLAIN)
            .body(resource);
  }
}
