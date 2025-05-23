package com.thedan17.salesnet.core.service;

import com.thedan17.salesnet.core.object.data.AsyncTaskInfo;
import com.thedan17.salesnet.exception.ContentNotFoundException;
import com.thedan17.salesnet.exception.RequestIgnoreNeededException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class DebugTaskService {
  private final Map<Integer, AsyncTaskInfo<LocalDate, Path>> tasks = new ConcurrentHashMap<>();
  private final AtomicInteger idCounter = new AtomicInteger(0);

  public DebugTaskService() {
    System.out.println("CONSTRUCTOR");
  }

  public void runLogTask(AsyncTaskInfo<LocalDate, Path> taskInfo) {
    taskInfo.setStatus(AsyncTaskInfo.Status.RUNNING);
    try {
      Thread.sleep(10000); // Long task imitation
      Path filePath = getLogByDate(taskInfo.getParams());

      taskInfo.setResult(filePath);
      taskInfo.setCompletedAt(Instant.now());
      if (filePath == null) {
        taskInfo.setStatus(AsyncTaskInfo.Status.FAILED);
      } else {
        taskInfo.setStatus(AsyncTaskInfo.Status.DONE);
      }
    } catch (InterruptedException e) {
      taskInfo.setStatus(AsyncTaskInfo.Status.FAILED);
      Thread.currentThread().interrupt();
    }
  }

  public Integer getLogNewTask(LocalDate date) {
    int taskId = idCounter.incrementAndGet();
    AsyncTaskInfo<LocalDate, Path> taskInfo = new AsyncTaskInfo<>(taskId, date);
    tasks.put(taskId, taskInfo);
    CompletableFuture.runAsync(
            () -> runLogTask(taskInfo)
    );
    return taskId;
  }

  public AsyncTaskInfo<LocalDate, Path> getTaskInfo(Integer taskId){
    AsyncTaskInfo<LocalDate, Path> taskInfo = tasks.get(taskId);
    if (taskInfo == null) {
      throw new ContentNotFoundException("Task with such ID don't exist");
    }
    return taskInfo;
  }

  public List<AsyncTaskInfo<LocalDate, Path>> getAllTasks() {
    return tasks.values().stream().toList();
  }

  public AsyncTaskInfo.Status getTaskStatus(Integer taskId) {
    return getTaskInfo(taskId).getStatus();
  }

  public Path getLogFile(Integer taskId) {
    AsyncTaskInfo<LocalDate, Path> taskInfo = getTaskInfo(taskId);
    if (taskInfo.getStatus() != AsyncTaskInfo.Status.DONE &&
        taskInfo.getStatus() != AsyncTaskInfo.Status.FAILED) {
      throw new RequestIgnoreNeededException("Task not ready yet, please wait and try again later");
    }
    return taskInfo.getResult();
  }

  private Path getLogByDate(LocalDate logDate) throws InterruptedException {
    String logDir = "./logs/"; // Путь к папке с логами
    String fileNamePattern = "log-" + logDate + ".log"; // Например, "log-2023-10-05.log"

    try (Stream<Path> paths = Files.list(Paths.get(logDir))) {
      Optional<Path> logPath = paths
              .filter(Files::isRegularFile)
              .filter(path -> path.getFileName().toString().equals(fileNamePattern))
              .findFirst();
      if (logPath.isPresent()) {
        return logPath.get();
      }
    } catch (IOException _) {}

    return null;
  }

  @Scheduled(fixedDelay = 30_000) // 30.000ms = 0.5min
  public void cleanupOldTasks() {
    try {
      System.out.println("CLEANUP RUN");
      Instant cutoff = Instant.now().minusSeconds(300); // 5 minutes
      tasks.entrySet().removeIf(entry -> {
        AsyncTaskInfo<LocalDate, Path> info = entry.getValue();
        info.calcDuration();
        System.out.println("CLEANUP RETURN");
        return (info.getStatus() == AsyncTaskInfo.Status.DONE ||
                info.getStatus() == AsyncTaskInfo.Status.FAILED ) &&
                info.getCompletedAt().isBefore(cutoff);
      });
    } catch (Exception e) {
      System.err.println("Cleanup task failed: " + e.getMessage());
      e.printStackTrace();
    }
    System.out.println("CLEANUP WHAT");
  }
}
