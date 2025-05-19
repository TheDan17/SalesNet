package com.thedan17.salesnet.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class UrlCountingInterceptor implements HandlerInterceptor {
  @Autowired
  private final UrlCounter counter;

  public UrlCountingInterceptor(UrlCounter counter) {
    this.counter = counter;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    counter.increment(pattern);
    return true;
  }
}
