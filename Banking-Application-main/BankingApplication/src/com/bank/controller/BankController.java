package com.bank.controller;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.service.BankService;

import java.util.List;
import java.util.Scanner;

public class BankController {
    private final BankService bankService;
    private final Scanner scanner;

    public BankController() {
        this.bankService = new BankService();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printWelcome();
        while (true) {
            printMainMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> handleCreateAccount();
                case 2 -> handleDeposit();
                case 3 -> handleWithdraw();
                case 4 -> handleTransfer();
                case 5 -> handleCheckBalance();
                case 6 -> handleViewAccount();
                case 7 -> handleTransactionHistory();
                case 8 -> handleViewAllAccounts();
                case 9 -> handleUpdateAccount();
                case 10 -> handleDeleteAccount();
                case 0 -> {
                    System.out.println("\nThank you for using the Banking Application. Goodbye!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void printWelcome() {
        System.out.println("==============================================");
        System.out.println("     WELCOME TO CONSOLE BANKING SYSTEM        ");
        System.out.println("==============================================");
    }

    private void printMainMenu() {
        System.out.println("\n----------------- MAIN MENU -----------------");
        System.out.println(" 1.  Create New Account");
        System.out.println(" 2.  Deposit Money");
        System.out.println(" 3.  Withdraw Money");
        System.out.println(" 4.  Transfer Money");
        System.out.println(" 5.  Check Balance");
        System.out.println(" 6.  View Account Details");
        System.out.println(" 7.  Transaction History");
        System.out.println(" 8.  View All Accounts");
        System.out.println(" 9.  Update Account");
        System.out.println(" 10. Delete Account");
        System.out.println(" 0.  Exit");
        System.out.println("---------------------------------------------");
    }

    private void handleCreateAccount() {
        System.out.println("\n--- Create New Account ---");
        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Phone (10 digits): ");
        String phone = scanner.nextLine().trim();

        double initialDeposit = readDouble("Enter Initial Deposit (min 500): ");

        System.out.print("Enter Account Type (SAVINGS/CURRENT): ");
        String type = scanner.nextLine().trim();

        int accountNumber = bankService.createAccount(name, email, phone, initialDeposit, type);
        if (accountNumber > 0) {
            System.out.println("\n✅ Account created successfully!");
            System.out.println("Your Account Number is: " + accountNumber);
            System.out.println("Please note it down for future transactions.");
        } else {
            System.out.println("\n❌ Account creation failed.");
        }
    }

    private void handleDeposit() {
        System.out.println("\n--- Deposit Money ---");
        int accNo = readInt("Enter Account Number: ");
        double amount = readDouble("Enter Deposit Amount: ");

        if (bankService.deposit(accNo, amount)) {
            System.out.printf("✅ Deposit successful! New Balance: %.2f%n", 
                    bankService.checkBalance(accNo));
        } else {
            System.out.println("❌ Deposit failed.");
        }
    }

    private void handleWithdraw() {
        System.out.println("\n--- Withdraw Money ---");
        int accNo = readInt("Enter Account Number: ");
        double amount = readDouble("Enter Withdrawal Amount: ");

        if (bankService.withdraw(accNo, amount)) {
            System.out.printf("✅ Withdrawal successful! New Balance: %.2f%n", 
                    bankService.checkBalance(accNo));
        } else {
            System.out.println("❌ Withdrawal failed.");
        }
    }

    private void handleTransfer() {
        System.out.println("\n--- Transfer Money ---");
        int fromAcc = readInt("Enter Source Account Number: ");
        int toAcc = readInt("Enter Destination Account Number: ");
        double amount = readDouble("Enter Transfer Amount: ");

        if (bankService.transfer(fromAcc, toAcc, amount)) {
            System.out.println("✅ Transfer successful!");
            System.out.printf("New Balance of Source Account: %.2f%n", 
                    bankService.checkBalance(fromAcc));
        } else {
            System.out.println("❌ Transfer failed.");
        }
    }

    private void handleCheckBalance() {
        System.out.println("\n--- Check Balance ---");
        int accNo = readInt("Enter Account Number: ");
        double balance = bankService.checkBalance(accNo);
        if (balance >= 0) {
            System.out.printf("💰 Available Balance: %.2f%n", balance);
        } else {
            System.out.println("❌ Account not found.");
        }
    }

    private void handleViewAccount() {
        System.out.println("\n--- View Account Details ---");
        int accNo = readInt("Enter Account Number: ");
        Account acc = bankService.getAccount(accNo);
        if (acc != null) {
            printAccountHeader();
            System.out.println(acc);
            printAccountFooter();
        } else {
            System.out.println("❌ Account not found.");
        }
    }

    private void handleTransactionHistory() {
        System.out.println("\n--- Transaction History ---");
        int accNo = readInt("Enter Account Number: ");
        Account acc = bankService.getAccount(accNo);
        if (acc == null) {
            System.out.println("❌ Account not found.");
            return;
        }
        List<Transaction> list = bankService.getTransactionHistory(accNo);
        if (list.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        System.out.println("+" + "-".repeat(96) + "+");
        System.out.printf("| %-8s | %-10s | %-12s | %-12s | %-15s | %-20s |%n",
                "TXN ID", "ACC NO", "TYPE", "AMOUNT", "BALANCE", "DATE");
        System.out.println("+" + "-".repeat(96) + "+");
        for (Transaction t : list) {
            System.out.println(t);
        }
        System.out.println("+" + "-".repeat(96) + "+");
    }

    private void handleViewAllAccounts() {
        System.out.println("\n--- All Accounts ---");
        List<Account> accounts = bankService.getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        printAccountHeader();
        for (Account a : accounts) {
            System.out.println(a);
        }
        printAccountFooter();
        System.out.println("Total Accounts: " + accounts.size());
    }

    private void handleUpdateAccount() {
        System.out.println("\n--- Update Account ---");
        int accNo = readInt("Enter Account Number to Update: ");
        Account existing = bankService.getAccount(accNo);
        if (existing == null) {
            System.out.println("❌ Account not found.");
            return;
        }

        System.out.print("Enter New Name (" + existing.getAccountHolderName() + "): ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter New Email (" + existing.getEmail() + "): ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter New Phone (" + existing.getPhone() + "): ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter New Account Type (" + existing.getAccountType() + "): ");
        String type = scanner.nextLine().trim();

        if (bankService.updateAccount(accNo, name, email, phone, type)) {
            System.out.println("✅ Account updated successfully!");
        } else {
            System.out.println("❌ Update failed.");
        }
    }

    private void handleDeleteAccount() {
        System.out.println("\n--- Delete Account ---");
        int accNo = readInt("Enter Account Number to Delete: ");
        System.out.print("Are you sure? (yes/no): ");
        String confirm = scanner.nextLine().trim();

        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Deletion cancelled.");
            return;
        }
        if (bankService.deleteAccount(accNo)) {
            System.out.println("✅ Account deleted successfully.");
        } else {
            System.out.println("❌ Deletion failed.");
        }
    }

    // ---- Helper methods ----
    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int val = Integer.parseInt(scanner.nextLine().trim());
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid amount.");
            }
        }
    }

    private void printAccountHeader() {
        System.out.println("+" + "-".repeat(102) + "+");
        System.out.printf("| %-10s | %-20s | %-25s | %-15s | %-12s | %-10s |%n",
                "ACC NO", "NAME", "EMAIL", "PHONE", "BALANCE", "TYPE");
        System.out.println("+" + "-".repeat(102) + "+");
    }

    private void printAccountFooter() {
        System.out.println("+" + "-".repeat(102) + "+");
    }
}