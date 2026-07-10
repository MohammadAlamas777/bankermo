package com.bankermo.bankermo.service;

import com.bankermo.bankermo.entity.Account;
import com.bankermo.bankermo.entity.Transaction;
import com.bankermo.bankermo.entity.User;
import com.bankermo.bankermo.repository.AccountRepository;
import com.bankermo.bankermo.repository.TransactionRepository;
import com.bankermo.bankermo.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
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
        Account saved = accountRepository.save(account);
        recordTransaction(saved, Transaction.TransactionType.DEPOSIT,
                amount, saved.getBalance(), "Deposit");
        return saved;
    }

    public Account withdraw(String userEmail, Long accountId, BigDecimal amount) {
        Account account = getOwnedAccount(userEmail, accountId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        Account saved = accountRepository.save(account);
        recordTransaction(saved, Transaction.TransactionType.WITHDRAWAL,
                amount, saved.getBalance(), "Withdrawal");
        return saved;
    }

    @Transactional
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
        recordTransaction(from, Transaction.TransactionType.TRANSFER_OUT,
                amount, from.getBalance(), "Transfer to " + to.getAccountNumber());
        recordTransaction(to, Transaction.TransactionType.TRANSFER_IN,
                amount, to.getBalance(), "Transfer from " + from.getAccountNumber());
    }

    private Account getOwnedAccount(String userEmail, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getOwner().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Account does not belong to you");
        }
        return account;
    }

    private void recordTransaction(Account account,
                                   Transaction.TransactionType type,
                                   BigDecimal amount,
                                   BigDecimal balanceAfter,
                                   String description) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(balanceAfter);
        tx.setDescription(description);
        transactionRepository.save(tx);
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "BM" + (1_000_000_000L + new Random().nextLong(9_000_000_000L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}