package com.bank.model;

public class Account {
    private int accountNumber;
    private String accountHolderName;
    private String email;
    private String phone;
    private double balance;
    private String accountType;

    public Account() {}

    public Account(String accountHolderName, String email, String phone, 
                   double balance, String accountType) {
        this.accountHolderName = accountHolderName;
        this.email = email;
        this.phone = phone;
        this.balance = balance;
        this.accountType = accountType;
    }

    // Getters and Setters
    public int getAccountNumber() { return accountNumber; }
    public void setAccountNumber(int accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    @Override
    public String toString() {
        return String.format("| %-10d | %-20s | %-25s | %-15s | %-12.2f | %-10s |",
                accountNumber, accountHolderName, email, phone, balance, accountType);
    }
}