package com.thedan17.salesnet.core.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import com.thedan17.salesnet.exception.ContentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DebugService {
  @Autowired private DebugTaskService debugTaskService;

  //public DebugService(DebugTaskService debugTaskService) {
  //  this.debugTaskService = debugTaskService;
  //}

  public Path getLogByDate(LocalDate logDate) {
    Path path = debugTaskService.getLogByDate(logDate);
    if (path == null) {
      throw new ContentNotFoundException("No such log file");
    }
    return path;
  }
}
