package com.thedan17.salesnet.controller;

import com.thedan17.salesnet.dao.AccountRepository;
import com.thedan17.salesnet.service.GroupSearchCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class GroupSearchController {
  @Autowired GroupSearchCacheService groupSearchCacheService;
  @Autowired AccountRepository accountRepository;

  @GetMapping("/search")
  public ResponseEntity<?> searchGroups(
      @RequestParam String name, @RequestParam(required = false) Long accId) {
    if (name.length() <= 3) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("To search groups by name, length of value must be more than 3");
    }
    if (accId != null && !accountRepository.existsById(accId)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body("Account with such id not exist in database");
    }
    return groupSearchCacheService
        .searchGroups(name, accId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
  }
}
