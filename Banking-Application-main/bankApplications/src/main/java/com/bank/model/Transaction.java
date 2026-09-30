package com.bank.model;

import java.sql.Timestamp;

public class Transaction {
    private int transactionId;
    private int accountNumber;
    private String transactionType;
    private double amount;
    private double balanceAfter;
    private Timestamp transactionDate;

    public Transaction() {}

    public Transaction(int accountNumber, String transactionType, 
                       double amount, double balanceAfter) {
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getAccountNumber() { return accountNumber; }
    public void setAccountNumber(int accountNumber) { this.accountNumber = accountNumber; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(double balanceAfter) { this.balanceAfter = balanceAfter; }

    public Timestamp getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Timestamp transactionDate) { this.transactionDate = transactionDate; }

    @Override
    public String toString() {
        return String.format("| %-8d | %-10d | %-12s | %-12.2f | %-15.2f | %-20s |",
                transactionId, accountNumber, transactionType, amount, 
                balanceAfter, transactionDate);
    }
}