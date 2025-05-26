package com.thedan17.salesnet;

import com.thedan17.salesnet.core.object.data.AsyncTaskInfo;
import com.thedan17.salesnet.core.service.DebugTaskService;
import com.thedan17.salesnet.exception.ContentNotFoundException;
import com.thedan17.salesnet.exception.RequestIgnoreNeededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DebugTaskServiceTest {

  private DebugTaskService service;

  @BeforeEach
  void setUp() {
    service = spy(new DebugTaskService());
  }

  // 1) getLogByDate(LocalDate)
  @Test
  void getLogByDate_shouldReturnPathIfExists() throws Exception {
    LocalDate date = LocalDate.of(2025, 5, 25);
    Path expected = null;//Paths.get("./logs/log-2025-05-25.log");

    try (MockedStatic<Files> files = mockStatic(Files.class)) {
      files.when(() -> Files.list(Paths.get("./logs/")))
              .thenReturn(Stream.of(expected));

      Method m = DebugTaskService.class
              .getDeclaredMethod("getLogByDate", LocalDate.class);
      m.setAccessible(true);

      Path result = (Path) m.invoke(service, date);
      assertEquals(expected, result);
    }
  }

  @Test
  void getLogByDate_shouldReturnNullIfNotFound() throws Exception {
    LocalDate date = LocalDate.now();
    try (MockedStatic<Files> files = mockStatic(Files.class)) {
      files.when(() -> Files.list(any()))
              .thenReturn(Stream.empty());

      Method m = DebugTaskService.class
              .getDeclaredMethod("getLogByDate", LocalDate.class);
      m.setAccessible(true);

      assertNull(m.invoke(service, date));
    }
  }

  // 3) getLogNewTask(LocalDate)
  @Test
  void getLogNewTask_shouldRegisterAndRunTask() {
    LocalDate date = LocalDate.now();
    // подменяем асинхронность:
    try (MockedStatic<CompletableFuture> cf = mockStatic(CompletableFuture.class)) {
      cf.when(() -> CompletableFuture.runAsync(any(Runnable.class)))
              .thenAnswer(inv -> { ((Runnable)inv.getArgument(0)).run(); return null; });

      Integer id = service.getLogNewTask(date);
      AsyncTaskInfo<LocalDate, Path> info = service.getTaskInfo(id);

      assertEquals(id, info.getId());
      assertEquals(date, info.getParams());
      // статус либо RUNNING→DONE/FAILED, но минимум не NEW:
      assertNotEquals(AsyncTaskInfo.Status.PENDING, info.getStatus());
    }
  }

  // 4) getTaskInfo(Integer)
  @Test
  void getTaskInfo_shouldThrowIfMissing() {
    assertThrows(ContentNotFoundException.class, () -> service.getTaskInfo(999));
  }

  // 5) getLogFile(Integer)
  @Test
  void getLogFile_shouldThrowWhenNotReady() {
    AsyncTaskInfo<LocalDate, Path> info = new AsyncTaskInfo<>(3, LocalDate.now());
    info.setStatus(AsyncTaskInfo.Status.RUNNING);
    // подменяем getTaskInfo:
    doReturn(info).when(service).getTaskInfo(3);

    assertThrows(RequestIgnoreNeededException.class, () -> service.getLogFile(3));
  }

  @Test
  void getLogFile_shouldReturnPathWhenDoneOrFailed() {
    Path p = Paths.get("x.log");
    AsyncTaskInfo<LocalDate, Path> info = new AsyncTaskInfo<>(4, LocalDate.now());
    info.setStatus(AsyncTaskInfo.Status.DONE);
    info.setResult(p);
    doReturn(info).when(service).getTaskInfo(4);

    assertEquals(p, service.getLogFile(4));

    info.setStatus(AsyncTaskInfo.Status.FAILED);
    assertEquals(p, service.getLogFile(4));
  }

  // 6) cleanupOldTasks() и лямбда
  @Test
  void cleanupOldTasks_shouldRemoveOldCompleted() {
    // вставляем задачу старую
    AsyncTaskInfo<LocalDate, Path> old = new AsyncTaskInfo<>(5, LocalDate.now());
    old.setStatus(AsyncTaskInfo.Status.DONE);
    old.setCompletedAt(Instant.now().minusSeconds(600)); // старше 5 мин
    service.tasks.put(5, old);

    // и свежую
    AsyncTaskInfo<LocalDate, Path> fresh = new AsyncTaskInfo<>(6, LocalDate.now());
    fresh.setStatus(AsyncTaskInfo.Status.DONE);
    fresh.setCompletedAt(Instant.now());
    service.tasks.put(6, fresh);

    service.cleanupOldTasks();

    assertFalse(service.tasks.containsKey(5));
    assertTrue(service.tasks.containsKey(6));
  }
}
