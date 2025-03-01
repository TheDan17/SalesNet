package com.thedan17.sales_net.controller;

import com.thedan17.sales_net.model.Account;
import com.thedan17.sales_net.service.AccountService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService service) {
        this.accountService = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id)
                .map(ResponseEntity::ok) // Если аккаунт найден, возвращаем 200 OK
                .orElse(ResponseEntity.notFound().build()); // Если нет, возвращаем 404 Not Found
    }

    @GetMapping("/search")
    public List<Account> searchAccounts(@RequestParam(required = false) String firstName,
                                        @RequestParam(required = false) String secondName,
                                        @RequestParam(required = false) String type) {
        return accountService.searchAccounts(firstName, secondName, type);
    }

    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        return accountService.addAccount(account);
    }

}
