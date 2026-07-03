package com.bankermo.bankermo.service;

import com.bankermo.bankermo.entity.Account;
import com.bankermo.bankermo.entity.User;
import com.bankermo.bankermo.repository.AccountRepository;
import com.bankermo.bankermo.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

    private String generateAccountNumber() {
        String number;
        do {
            number = "BM" + (1_000_000_000L + new Random().nextLong(9_000_000_000L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}