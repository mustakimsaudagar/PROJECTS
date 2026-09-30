package com.bank.dao.impl;

import com.bank.dao.AccountDAO;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {

    @Override
    public int createAccount(Account account) {
        String sql = "INSERT INTO accounts (account_holder_name, email, phone, balance, account_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, account.getAccountHolderName());
            ps.setString(2, account.getEmail());
            ps.setString(3, account.getPhone());
            ps.setDouble(4, account.getBalance());
            ps.setString(5, account.getAccountType());
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public Account getAccountByNumber(int accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching account: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Account getAccountByEmail(String email) {
        String sql = "SELECT * FROM accounts WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching account by email: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY account_number";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all accounts: " + e.getMessage());
        }
        return accounts;
    }

    @Override
    public boolean updateAccount(Account account) {
        String sql = "UPDATE accounts SET account_holder_name=?, email=?, phone=?, account_type=? WHERE account_number=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, account.getAccountHolderName());
            ps.setString(2, account.getEmail());
            ps.setString(3, account.getPhone());
            ps.setString(4, account.getAccountType());
            ps.setInt(5, account.getAccountNumber());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating account: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteAccount(int accountNumber) {
        String sql = "DELETE FROM accounts WHERE account_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, accountNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deposit(int accountNumber, double amount) {
        String updateSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
        String txnSql = "INSERT INTO transactions (account_number, transaction_type, amount, balance_after) VALUES (?, 'DEPOSIT', ?, ?)";
        
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Update balance
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, accountNumber);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    return false;
                }
            }

            // Get new balance
            double newBalance = getBalance(accountNumber);

            // Insert transaction record
            try (PreparedStatement ps = con.prepareStatement(txnSql)) {
                ps.setInt(1, accountNumber);
                ps.setDouble(2, amount);
                ps.setDouble(3, newBalance);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Error depositing: " + e.getMessage());
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    @Override
    public boolean withdraw(int accountNumber, double amount) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Check balance
            double currentBalance = getBalance(accountNumber);
            if (currentBalance < amount) {
                System.err.println("Insufficient balance!");
                con.rollback();
                return false;
            }

            // Update balance
            String updateSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, accountNumber);
                ps.executeUpdate();
            }

            double newBalance = currentBalance - amount;

            // Insert transaction record
            String txnSql = "INSERT INTO transactions (account_number, transaction_type, amount, balance_after) VALUES (?, 'WITHDRAW', ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(txnSql)) {
                ps.setInt(1, accountNumber);
                ps.setDouble(2, amount);
                ps.setDouble(3, newBalance);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Error withdrawing: " + e.getMessage());
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    @Override
    public boolean transfer(int fromAccount, int toAccount, double amount) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Check balance
            double fromBalance = getBalance(fromAccount);
            if (fromBalance < amount) {
                System.err.println("Insufficient balance for transfer!");
                con.rollback();
                return false;
            }

            // Check if toAccount exists
            if (getAccountByNumber(toAccount) == null) {
                System.err.println("Destination account not found!");
                con.rollback();
                return false;
            }

            // Debit from source
            String debitSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            try (PreparedStatement ps = con.prepareStatement(debitSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, fromAccount);
                ps.executeUpdate();
            }

            // Credit to destination
            String creditSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            try (PreparedStatement ps = con.prepareStatement(creditSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, toAccount);
                ps.executeUpdate();
            }

            // Record transactions
            double newFromBalance = fromBalance - amount;
            double newToBalance = getBalance(toAccount);

            String txnSql = "INSERT INTO transactions (account_number, transaction_type, amount, balance_after) VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement ps = con.prepareStatement(txnSql)) {
                ps.setInt(1, fromAccount);
                ps.setString(2, "TRANSFER_OUT");
                ps.setDouble(3, amount);
                ps.setDouble(4, newFromBalance);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(txnSql)) {
                ps.setInt(1, toAccount);
                ps.setString(2, "TRANSFER_IN");
                ps.setDouble(3, amount);
                ps.setDouble(4, newToBalance);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Error transferring: " + e.getMessage());
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    @Override
    public List<Transaction> getTransactionHistory(int accountNumber) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setTransactionId(rs.getInt("transaction_id"));
                t.setAccountNumber(rs.getInt("account_number"));
                t.setTransactionType(rs.getString("transaction_type"));
                t.setAmount(rs.getDouble("amount"));
                t.setBalanceAfter(rs.getDouble("balance_after"));
                t.setTransactionDate(rs.getTimestamp("transaction_date"));
                transactions.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return transactions;
    }

    @Override
    public double getBalance(int accountNumber) {
        String sql = "SELECT balance FROM accounts WHERE account_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching balance: " + e.getMessage());
        }
        return -1;
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountNumber(rs.getInt("account_number"));
        account.setAccountHolderName(rs.getString("account_holder_name"));
        account.setEmail(rs.getString("email"));
        account.setPhone(rs.getString("phone"));
        account.setBalance(rs.getDouble("balance"));
        account.setAccountType(rs.getString("account_type"));
        return account;
    }
}