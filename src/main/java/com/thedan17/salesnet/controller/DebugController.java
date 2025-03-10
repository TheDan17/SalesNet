package com.thedan17.salesnet.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Контроллер для отладочных действий на время разработки до релиза. */
@RestController
@RequestMapping("/api/debug")
public class DebugController {
  @Autowired private EntityManagerFactory entityManagerFactory;

  /** Метод, который должен очищать кэш 2L-уровня (сохраняемый между сессиями).*/
  @PostMapping("/clear-cache")
  public ResponseEntity<String> clearFirstLevelCache() {
    entityManagerFactory.getCache().evictAll();
    return ResponseEntity.ok("Кэш очищен");
  }
}
