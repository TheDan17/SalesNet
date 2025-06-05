package com.thedan17.salesnet.core.service;

import com.thedan17.salesnet.exception.ContentNotFoundException;
import java.nio.file.Path;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DebugService {
  @Autowired private DebugTaskService debugTaskService;

  public Path getLogByDate(LocalDate logDate) {
    Path path = debugTaskService.getLogByDate(logDate);
    if (path == null) {
      throw new ContentNotFoundException("No such log file");
    }
    return path;
  }
}
