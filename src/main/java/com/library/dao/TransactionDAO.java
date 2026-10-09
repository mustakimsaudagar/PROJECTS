package com.library.dao;

import com.library.entity.Book;
import com.library.entity.Transaction;
import com.library.entity.User;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public class TransactionDAO {

    public static final double DEFAULT_FINE_PER_DAY = 2.00;

    /**
     * Issues a book to a user if stock is available.
     * Decrements availableCopies and saves the transaction atomically.
     */
    public boolean issueBook(Long userId, Long bookId, int loanDays, String remarks) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User user = session.get(User.class, userId);
            Book book = session.get(Book.class, bookId);

            if (user == null || book == null) {
                System.err.println("User or Book not found for checkout");
                return false;
            }

            if (book.getAvailableCopies() <= 0) {
                System.err.println("No available copies of " + book.getTitle());
                return false;
            }

            // Decrement stock
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            session.merge(book);

            // Create Transaction record
            LocalDate now = LocalDate.now();
            int days = (loanDays > 0) ? loanDays : 14; // Default 14 days
            LocalDate due = now.plusDays(days);

            Transaction record = new Transaction();
            record.setUser(user);
            record.setBook(book);
            record.setIssueDate(now);
            record.setDueDate(due);
            record.setStatus("ISSUED");
            record.setFineAmount(0.00);
            record.setRemarks(remarks != null ? remarks : "Issued successfully");

            session.persist(record);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns an issued book.
     * Calculates any overdue fine, marks status RETURNED, and increments availableCopies.
     */
    public boolean returnBook(Long transactionId, Double customFine, String remarks) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Transaction record = session.get(Transaction.class, transactionId);
            if (record == null || "RETURNED".equalsIgnoreCase(record.getStatus())) {
                return false;
            }

            Book book = record.getBook();
            LocalDate today = LocalDate.now();
            record.setReturnDate(today);

            // Fine calculation
            double fine = 0.00;
            if (customFine != null && customFine >= 0) {
                fine = customFine;
            } else if (today.isAfter(record.getDueDate())) {
                long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), today);
                fine = overdueDays * DEFAULT_FINE_PER_DAY;
            }

            record.setFineAmount(fine);
            record.setStatus("RETURNED");
            if (remarks != null && !remarks.trim().isEmpty()) {
                record.setRemarks(remarks);
            }

            // Increment available copies
            if (book != null) {
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                session.merge(book);
            }

            session.merge(record);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public Transaction getTransactionById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Transaction.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Transaction> getAllTransactions() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Refresh overdue flags dynamically
            Query<Transaction> query = session.createQuery(
                    "SELECT t FROM Transaction t JOIN FETCH t.user JOIN FETCH t.book ORDER BY t.id DESC", Transaction.class);
            List<Transaction> list = query.list();
            checkAndFlagOverdue(list);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Transaction> getTransactionsByUser(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Transaction> query = session.createQuery(
                    "SELECT t FROM Transaction t JOIN FETCH t.book WHERE t.user.id = :userId ORDER BY t.id DESC", Transaction.class);
            query.setParameter("userId", userId);
            List<Transaction> list = query.list();
            checkAndFlagOverdue(list);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Transaction> getActiveTransactions() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Transaction> query = session.createQuery(
                    "SELECT t FROM Transaction t JOIN FETCH t.user JOIN FETCH t.book WHERE t.status != 'RETURNED' ORDER BY t.dueDate ASC", Transaction.class);
            List<Transaction> list = query.list();
            checkAndFlagOverdue(list);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public long countActiveLoans() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(t) FROM Transaction t WHERE t.status != 'RETURNED'", Long.class);
            return query.uniqueResult() != null ? query.uniqueResult() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public double sumTotalFines() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                    "SELECT COALESCE(SUM(t.fineAmount), 0.0) FROM Transaction t", Double.class);
            return query.uniqueResult() != null ? query.uniqueResult() : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public long countOverdueTransactions() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(t) FROM Transaction t WHERE t.status != 'RETURNED' AND t.dueDate < :today", Long.class);
            query.setParameter("today", LocalDate.now());
            return query.uniqueResult() != null ? query.uniqueResult() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void checkAndFlagOverdue(List<Transaction> list) {
        LocalDate today = LocalDate.now();
        for (Transaction t : list) {
            if (!"RETURNED".equalsIgnoreCase(t.getStatus()) && today.isAfter(t.getDueDate())) {
                t.setStatus("OVERDUE");
                long days = ChronoUnit.DAYS.between(t.getDueDate(), today);
                t.setFineAmount(days * DEFAULT_FINE_PER_DAY);
            }
        }
    }
}
