package com.thedan17.salesnet.core.controller;

import com.thedan17.salesnet.core.object.data.AsyncTaskInfo;
import com.thedan17.salesnet.core.service.DebugTaskService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

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

  @PostMapping("/log/create_task")
  public ResponseEntity<Integer> createGetLogTask(@RequestParam Short year,
                                                  @RequestParam Short month,
                                                  @RequestParam Short day) {
    LocalDate taskParams;
    try {
      taskParams = LocalDate.of(year, month, day);
    } catch (DateTimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(-1);
    }
    return ResponseEntity.ok(debugTaskService.getLogNewTask(taskParams));
  }

  @GetMapping("/log/get_all_tasks")
  public ResponseEntity<List<AsyncTaskInfo<LocalDate, Path>>> getAllTasks() {
    return ResponseEntity.ok(debugTaskService.getAllTasks());
  }

  @GetMapping("/log/check_task")
  public ResponseEntity<AsyncTaskInfo.Status> getStatus(@RequestParam Integer taskId) {
    return ResponseEntity.ok(debugTaskService.getTaskStatus(taskId));
  }

  @GetMapping("/log/get_result")
  public ResponseEntity<Resource> downloadFile(@RequestParam Integer taskId) {
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
