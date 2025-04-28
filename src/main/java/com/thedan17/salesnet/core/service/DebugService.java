package com.thedan17.salesnet.core.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class DebugService {
  public Optional<String> getLogByDate(LocalDate logDate) {
    String logDir = "./logs/"; // Путь к папке с логами
    String fileNamePattern = "log-" + logDate + ".log"; // Например, "log-2023-10-05.log"

    try (Stream<Path> paths = Files.list(Paths.get(logDir))) {
      Optional<Path> logPath = paths
              .filter(Files::isRegularFile)
              .filter(path -> path.getFileName().toString().equals(fileNamePattern))
              .findFirst();
      if (logPath.isPresent()) {
        return Optional.of(Files.readString(logPath.get()));
      }
    } catch (IOException e) {
      return Optional.empty();
    }
    return Optional.empty();
  }
}
