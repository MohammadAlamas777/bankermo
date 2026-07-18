package com.bankermo.bankermo.controller;

import com.bankermo.bankermo.dto.AmountRequest;
import com.bankermo.bankermo.dto.TransferRequest;
import com.bankermo.bankermo.entity.Account;
import com.bankermo.bankermo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.bankermo.bankermo.entity.Transaction;

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
    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(
            Authentication authentication,
            @PathVariable Long accountId) {
        Account account = accountService.getOwnedAccount(
                authentication.getName(), accountId);
        return ResponseEntity.ok(account);
    }
    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<Account> deposit(
            Authentication authentication,
            @PathVariable Long accountId,
            @Valid @RequestBody AmountRequest request) {
        Account account = accountService.deposit(authentication.getName(), accountId, request.amount());
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<Account> withdraw(
            Authentication authentication,
            @PathVariable Long accountId,
            @Valid @RequestBody AmountRequest request) {
        Account account = accountService.withdraw(authentication.getName(), accountId, request.amount());
        return ResponseEntity.ok(account);
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request) {
        accountService.transfer(
                authentication.getName(),
                request.fromAccountId(),
                request.toAccountId(),
                request.amount()
        );
        return ResponseEntity.ok("Transfer successful");
    }
    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(
            Authentication authentication,
            @PathVariable Long accountId) {
        List<Transaction> transactions = accountService.getTransactions(
                authentication.getName(), accountId);
        return ResponseEntity.ok(transactions);
    }
}