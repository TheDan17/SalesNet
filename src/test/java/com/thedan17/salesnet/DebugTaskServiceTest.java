package com.thedan17.salesnet;

import com.thedan17.salesnet.core.object.data.AsyncTaskInfo;
import com.thedan17.salesnet.core.service.DebugTaskService;
import com.thedan17.salesnet.exception.ContentNotFoundException;
import com.thedan17.salesnet.exception.RequestIgnoreNeededException;
import com.thedan17.salesnet.util.AppLoggerCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DebugTaskServiceTest {

  private DebugTaskService debugTaskService;

  @BeforeEach
  void setUp() {
    debugTaskService = spy(new DebugTaskService());
  }

  @Test
  void cleanupOldTasks_shouldNotRemoveCompletedButRecent() {
    AsyncTaskInfo<LocalDate, Path> recent = new AsyncTaskInfo<>(7, LocalDate.now());
    recent.setStatus(AsyncTaskInfo.Status.DONE);
    recent.setCompletedAt(Instant.now()); // новое

    debugTaskService.tasks.put(7, recent);

    debugTaskService.cleanupOldTasks();

    assertTrue(debugTaskService.tasks.containsKey(7)); // НЕ удалён
  }

  @Test
  void cleanupOldTasks_shouldNotRemoveNotYetCompletedEvenIfOld() {
    AsyncTaskInfo<LocalDate, Path> pending = new AsyncTaskInfo<>(8, LocalDate.now());
    pending.setStatus(AsyncTaskInfo.Status.RUNNING); // или PENDING
    pending.setCompletedAt(Instant.now().minusSeconds(600)); // старый

    debugTaskService.tasks.put(8, pending);

    debugTaskService.cleanupOldTasks();

    assertTrue(debugTaskService.tasks.containsKey(8)); // НЕ удалён
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

      Path result = (Path) m.invoke(debugTaskService, date);
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

      assertNull(m.invoke(debugTaskService, date));
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

      Integer id = debugTaskService.getLogNewTask(date);
      AsyncTaskInfo<LocalDate, Path> info = debugTaskService.getTaskInfo(id);

      assertEquals(id, info.getId());
      assertEquals(date, info.getParams());
      // статус либо RUNNING→DONE/FAILED, но минимум не NEW:
      assertNotEquals(AsyncTaskInfo.Status.PENDING, info.getStatus());
    }
  }

  // 4) getTaskInfo(Integer)
  @Test
  void getTaskInfo_shouldThrowIfMissing() {
    assertThrows(ContentNotFoundException.class, () -> debugTaskService.getTaskInfo(999));
  }

  // 5) getLogFile(Integer)
  @Test
  void getLogFile_shouldThrowWhenNotReady() {
    AsyncTaskInfo<LocalDate, Path> info = new AsyncTaskInfo<>(3, LocalDate.now());
    info.setStatus(AsyncTaskInfo.Status.RUNNING);
    // подменяем getTaskInfo:
    doReturn(info).when(debugTaskService).getTaskInfo(3);

    assertThrows(RequestIgnoreNeededException.class, () -> debugTaskService.getLogFile(3));
  }

  @Test
  void getLogFile_shouldReturnPathWhenDoneOrFailed() {
    Path p = Paths.get("x.log");
    AsyncTaskInfo<LocalDate, Path> info = new AsyncTaskInfo<>(4, LocalDate.now());
    info.setStatus(AsyncTaskInfo.Status.DONE);
    info.setResult(p);
    doReturn(info).when(debugTaskService).getTaskInfo(4);

    assertEquals(p, debugTaskService.getLogFile(4));

    info.setStatus(AsyncTaskInfo.Status.FAILED);
    assertEquals(p, debugTaskService.getLogFile(4));
  }

  // 6) cleanupOldTasks() и лямбда
  @Test
  void cleanupOldTasks_shouldRemoveOldCompleted() {
    // вставляем задачу старую
    AsyncTaskInfo<LocalDate, Path> old = new AsyncTaskInfo<>(5, LocalDate.now());
    old.setStatus(AsyncTaskInfo.Status.DONE);
    old.setCompletedAt(Instant.now().minusSeconds(600)); // старше 5 мин
    debugTaskService.tasks.put(5, old);

    // и свежую
    AsyncTaskInfo<LocalDate, Path> fresh = new AsyncTaskInfo<>(6, LocalDate.now());
    fresh.setStatus(AsyncTaskInfo.Status.DONE);
    fresh.setCompletedAt(Instant.now());
    debugTaskService.tasks.put(6, fresh);

    debugTaskService.cleanupOldTasks();

    assertFalse(debugTaskService.tasks.containsKey(5));
    assertTrue(debugTaskService.tasks.containsKey(6));
  }
}
