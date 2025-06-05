package com.thedan17.salesnet.core.service;

import com.thedan17.salesnet.util.UrlCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Optional;

@Service
public class CounterService {
  @Autowired
  private final UrlCounter urlCounter;
  @Autowired
  private RequestMappingHandlerMapping requestMappingHandlerMapping;

  public CounterService (UrlCounter urlCounter) {
    this.urlCounter = urlCounter;
  }

  public Map<String, Integer> getAllUrlCounters() {
    return urlCounter.getAllCounts();
  }

  public boolean isEndpointPatternRegistered(String uriPattern) {
    if (requestMappingHandlerMapping == null) {
      return false;
    }
    return requestMappingHandlerMapping
            .getHandlerMethods()
            .keySet()
            .stream()
            .flatMap(info -> info.getPatternValues().stream())
            .anyMatch(pattern -> pattern.equals(uriPattern));
  }

  public Optional<Pair<String, Integer>> getCurrentUrlCounter(String url) {
    int result = urlCounter.getCount(url);
    if (result == -1) {
      if (isEndpointPatternRegistered(url)) {
        return Optional.of(Pair.of(url, 0));
      }
      return Optional.empty();
    }
    return Optional.of(Pair.of(url, result));
  }
}
