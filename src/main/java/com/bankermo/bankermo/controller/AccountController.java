package com.bankermo.bankermo.controller;

import com.bankermo.bankermo.entity.Account;
import com.bankermo.bankermo.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(Authentication authentication) {
        Account account = accountService.createAccount(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping
    public ResponseEntity<List<Account>> getMyAccounts(Authentication authentication) {
        List<Account> accounts = accountService.getMyAccounts(authentication.getName());
        return ResponseEntity.ok(accounts);
    }
}