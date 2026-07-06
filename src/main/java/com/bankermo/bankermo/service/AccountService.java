package com.bankermo.bankermo.service;

import com.bankermo.bankermo.entity.Account;
import com.bankermo.bankermo.entity.User;
import com.bankermo.bankermo.repository.AccountRepository;
import com.bankermo.bankermo.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Account createAccount(String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setOwner(owner);

        return accountRepository.save(account);
    }

    public List<Account> getMyAccounts(String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return accountRepository.findByOwnerId(owner.getId());
    }

    public Account deposit(String userEmail, Long accountId, BigDecimal amount) {
        Account account = getOwnedAccount(userEmail, accountId);
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    public Account withdraw(String userEmail, Long accountId, BigDecimal amount) {
        Account account = getOwnedAccount(userEmail, accountId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        return accountRepository.save(account);
    }

    private Account getOwnedAccount(String userEmail, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getOwner().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Account does not belong to you");
        }
        return account;
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "BM" + (1_000_000_000L + new Random().nextLong(9_000_000_000L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    @org.springframework.transaction.annotation.Transactional
    public void transfer(String userEmail, Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account from = getOwnedAccount(userEmail, fromAccountId);
        Account to = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);
    }
}