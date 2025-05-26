package com.thedan17.salesnet;

import com.thedan17.salesnet.core.service.DebugService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebugServiceTest {

  DebugService debugService;

  @BeforeEach
  void setUp() {
    debugService = new DebugService();
  }

  @Test
  void getLogByDate_shouldReturnContent_WhenLogFileExists() throws IOException {
    LocalDate date = LocalDate.of(2023, 10, 5);
    String fileName = "log-" + date + ".log";
    Path mockPath = Paths.get("./logs/" + fileName);
    String expectedContent = "Some log content";

    try (MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
      filesMockedStatic
              .when(() -> Files.list(Paths.get("./logs/")))
              .thenReturn(Stream.of(mockPath));

      filesMockedStatic
              .when(() -> Files.isRegularFile(mockPath))
              .thenReturn(true);

      filesMockedStatic
              .when(() -> Files.readString(mockPath))
              .thenReturn(expectedContent);

      Optional<String> result = debugService.getLogByDate(date);

      assertTrue(result.isPresent());
      assertEquals(expectedContent, result.get());
    }
  }

  @Test
  void getLogByDate_shouldReturnEmpty_WhenNoFileMatches() throws IOException {
    LocalDate date = LocalDate.of(2023, 10, 5);
    Path anotherPath = Paths.get("./logs/some-other-file.log");

    try (MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
      filesMockedStatic
              .when(() -> Files.list(Paths.get("./logs/")))
              .thenReturn(Stream.of(anotherPath));

      filesMockedStatic
              .when(() -> Files.isRegularFile(anotherPath))
              .thenReturn(true);

      Optional<String> result = debugService.getLogByDate(date);

      assertTrue(result.isEmpty());
    }
  }

  @Test
  void getLogByDate_shouldReturnEmpty_WhenIOExceptionOccurs() throws IOException {
    LocalDate date = LocalDate.now();

    try (MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
      filesMockedStatic
              .when(() -> Files.list(Paths.get("./logs/")))
              .thenThrow(new IOException("Simulated failure"));

      Optional<String> result = debugService.getLogByDate(date);

      assertTrue(result.isEmpty());
    }
  }
}
