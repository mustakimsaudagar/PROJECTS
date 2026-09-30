package com.bank.dao;

import com.bank.model.Account;
import com.bank.model.Transaction;
import java.util.List;

public interface AccountDAO {
    int createAccount(Account account);
    Account getAccountByNumber(int accountNumber);
    Account getAccountByEmail(String email);
    List<Account> getAllAccounts();
    boolean updateAccount(Account account);
    boolean deleteAccount(int accountNumber);
    boolean deposit(int accountNumber, double amount);
    boolean withdraw(int accountNumber, double amount);
    boolean transfer(int fromAccount, int toAccount, double amount);
    List<Transaction> getTransactionHistory(int accountNumber);
    double getBalance(int accountNumber);
}