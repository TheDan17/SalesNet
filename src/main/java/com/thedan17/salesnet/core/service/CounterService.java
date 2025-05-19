package com.thedan17.salesnet.core.service;

import com.thedan17.salesnet.util.UrlCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class CounterService {
  @Autowired
  private final UrlCounter urlCounter;

  public CounterService (UrlCounter urlCounter) {
    this.urlCounter = urlCounter;
  }

  public Map<String, Integer> getAllUrlCounters() {
    return urlCounter.getAllCounts();
  }

  public Optional<Pair<String, Integer>> getCurrentUrlCounter(String url) {
    int result = urlCounter.getCount(url);
    if (result == -1) {
      return Optional.empty();
    }
    return Optional.of(Pair.of(url, result));
  }
}
