package com.thedan17.salesnet;

import com.thedan17.salesnet.core.service.DebugService;
import com.thedan17.salesnet.core.service.DebugTaskService;
import com.thedan17.salesnet.exception.ContentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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
  @Mock
  private DebugTaskService debugTaskService;

  @InjectMocks
  private DebugService debugService;

  /*
  @BeforeEach
  void setUp() {
    debugService = new DebugService();
  }
  */

  @Test
  void getLogByDate_shouldReturnContent_WhenLogFileExists() throws IOException {
    LocalDate date = LocalDate.of(2023, 10, 5);
    String fileName = "log-" + date + ".log";
    Path mockPath = Paths.get("./logs/" + fileName);
    Path expectedContent = Path.of("path/to/file.txt");

    when(debugTaskService.getLogByDate(date)).thenReturn(Path.of("path/to/file.txt"));
    Path result = debugService.getLogByDate(date);

    assertNotNull(result);
    assertEquals(expectedContent, result);
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

      when(debugTaskService.getLogByDate(date)).thenReturn(null);

      assertThrows(ContentNotFoundException.class, ()->debugService.getLogByDate(date));
    }
  }

  /*
  @Test
  void getLogByDate_shouldReturnEmpty_WhenIOExceptionOccurs() throws IOException {
    LocalDate date = LocalDate.now();

    try (MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
      filesMockedStatic
              .when(() -> Files.list(Paths.get("./logs/")))
              .thenThrow(new IOException("Simulated failure"));

      when(debugTaskService.getLogByDate(date)).thenReturn(null);
      Path result = debugService.getLogByDate(date);

      assertNull(result);
    }
  }
  */
}
