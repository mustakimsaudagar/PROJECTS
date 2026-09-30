package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.impl.AccountDAOImpl;
import com.bank.model.Account;
import com.bank.model.Transaction;

import java.util.List;
import java.util.regex.Pattern;

public class BankService {
    private final AccountDAO accountDAO;

    public BankService() {
        this.accountDAO = new AccountDAOImpl();
    }

    // Validation helpers
    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(regex, email);
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }

    public int createAccount(String name, String email, String phone, 
                             double initialDeposit, String accountType) {
        // Validations
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Name cannot be empty.");
            return -1;
        }
        if (!isValidEmail(email)) {
            System.err.println("Invalid email format.");
            return -1;
        }
        if (!isValidPhone(phone)) {
            System.err.println("Invalid phone number. Must be 10 digits.");
            return -1;
        }
        if (initialDeposit < 500) {
            System.err.println("Minimum initial deposit is 500.");
            return -1;
        }
        if (!accountType.equalsIgnoreCase("SAVINGS") && !accountType.equalsIgnoreCase("CURRENT")) {
            System.err.println("Account type must be SAVINGS or CURRENT.");
            return -1;
        }

        // Check duplicate email
        if (accountDAO.getAccountByEmail(email) != null) {
            System.err.println("Account with this email already exists.");
            return -1;
        }

        Account account = new Account(name, email, phone, initialDeposit, accountType.toUpperCase());
        int accountNumber = accountDAO.createAccount(account);

        if (accountNumber > 0) {
            // Record the initial deposit transaction
            accountDAO.deposit(accountNumber, 0); // creates txn record with 0? Better to insert manually
        }
        return accountNumber;
    }

    public Account getAccount(int accountNumber) {
        return accountDAO.getAccountByNumber(accountNumber);
    }

    public List<Account> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }

    public boolean updateAccount(int accountNumber, String name, String email, String phone, String accountType) {
        Account existing = accountDAO.getAccountByNumber(accountNumber);
        if (existing == null) {
            System.err.println("Account not found.");
            return false;
        }
        if (!isValidEmail(email)) {
            System.err.println("Invalid email format.");
            return false;
        }
        if (!isValidPhone(phone)) {
            System.err.println("Invalid phone number.");
            return false;
        }

        existing.setAccountHolderName(name);
        existing.setEmail(email);
        existing.setPhone(phone);
        existing.setAccountType(accountType.toUpperCase());
        return accountDAO.updateAccount(existing);
    }

    public boolean deleteAccount(int accountNumber) {
        Account existing = accountDAO.getAccountByNumber(accountNumber);
        if (existing == null) {
            System.err.println("Account not found.");
            return false;
        }
        if (existing.getBalance() > 0) {
            System.err.println("Cannot delete account with non-zero balance. Please withdraw first.");
            return false;
        }
        return accountDAO.deleteAccount(accountNumber);
    }

    public boolean deposit(int accountNumber, double amount) {
        if (amount <= 0) {
            System.err.println("Deposit amount must be positive.");
            return false;
        }
        if (accountDAO.getAccountByNumber(accountNumber) == null) {
            System.err.println("Account not found.");
            return false;
        }
        return accountDAO.deposit(accountNumber, amount);
    }

    public boolean withdraw(int accountNumber, double amount) {
        if (amount <= 0) {
            System.err.println("Withdrawal amount must be positive.");
            return false;
        }
        Account acc = accountDAO.getAccountByNumber(accountNumber);
        if (acc == null) {
            System.err.println("Account not found.");
            return false;
        }
        if (acc.getBalance() < amount) {
            System.err.println("Insufficient balance. Available: " + acc.getBalance());
            return false;
        }
        return accountDAO.withdraw(accountNumber, amount);
    }

    public boolean transfer(int fromAccount, int toAccount, double amount) {
        if (amount <= 0) {
            System.err.println("Transfer amount must be positive.");
            return false;
        }
        if (fromAccount == toAccount) {
            System.err.println("Cannot transfer to the same account.");
            return false;
        }
        if (accountDAO.getAccountByNumber(fromAccount) == null) {
            System.err.println("Source account not found.");
            return false;
        }
        if (accountDAO.getAccountByNumber(toAccount) == null) {
            System.err.println("Destination account not found.");
            return false;
        }
        return accountDAO.transfer(fromAccount, toAccount, amount);
    }

    public double checkBalance(int accountNumber) {
        return accountDAO.getBalance(accountNumber);
    }

    public List<Transaction> getTransactionHistory(int accountNumber) {
        return accountDAO.getTransactionHistory(accountNumber);
    }
}