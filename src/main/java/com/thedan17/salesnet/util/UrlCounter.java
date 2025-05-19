package com.thedan17.salesnet.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UrlCounter {
  private final ConcurrentMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

  public void increment(String path) {
    counters.computeIfAbsent(path, p -> new AtomicInteger(0)).incrementAndGet();
  }

  public int getCount(String path) {
    return counters.getOrDefault(path, new AtomicInteger(-1)).get();
  }

  public Map<String, Integer> getAllCounts() {
    return counters.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().get()
            ));
  }
}
