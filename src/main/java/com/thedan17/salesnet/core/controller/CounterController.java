package com.thedan17.salesnet.core.controller;

import com.thedan17.salesnet.core.service.CounterService;
import com.thedan17.salesnet.exception.ContentNotFoundException;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/stats/url")
public class CounterController {
  @Autowired
  private final CounterService counterService;

  public CounterController(CounterService counterService){
    this.counterService = counterService;
  }

  @GetMapping
  ResponseEntity<Map<String, Integer>> getAllCounters(@RequestParam(required = false) String path) {
    if (path == null) {
      return ResponseEntity
              .status(HttpStatus.OK)
              .body(counterService.getAllUrlCounters());
    }
    var counter = counterService.getCurrentUrlCounter(path);
    if (counter.isPresent()) {
      Map<String, Integer> founded = new HashMap<>();
      founded.put(counter.get().getFirst(), counter.get().getSecond());
      return ResponseEntity.ok(founded);
    }
    throw new ContentNotFoundException("Optional with url counter is empty");
  }
}
