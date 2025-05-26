package com.thedan17.salesnet;

import com.thedan17.salesnet.core.service.CounterService;
import com.thedan17.salesnet.util.UrlCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.util.Pair;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CounterServiceTest {

  private UrlCounter urlCounterMock;
  private CounterService counterService;

  @BeforeEach
  void setUp() {
    urlCounterMock = mock(UrlCounter.class);
    counterService = new CounterService(urlCounterMock);
  }

  @Test
  void getAllUrlCounters_shouldReturnCorrectMap() {
    Map<String, Integer> mockMap = Map.of("google.com", 10, "openai.com", 5);
    when(urlCounterMock.getAllCounts()).thenReturn(mockMap);

    Map<String, Integer> result = counterService.getAllUrlCounters();
    assertEquals(mockMap, result);
    verify(urlCounterMock, times(1)).getAllCounts();
  }

  @Test
  void getCurrentUrlCounter_shouldReturnPair_whenCounterExists() {
    when(urlCounterMock.getCount("openai.com")).thenReturn(42);

    Optional<Pair<String, Integer>> result =
        counterService.getCurrentUrlCounter("openai.com");

    assertTrue(result.isPresent());
    assertEquals("openai.com", result.get().getFirst());
    assertEquals(42, result.get().getSecond());
    verify(urlCounterMock).getCount("openai.com");
  }

  @Test
  void getCurrentUrlCounter_shouldReturnEmpty_whenCounterMissing() {
    when(urlCounterMock.getCount("unknown.com")).thenReturn(-1);

    Optional<Pair<String, Integer>> result = counterService.getCurrentUrlCounter("unknown.com");

    assertTrue(result.isEmpty());
  }
}
