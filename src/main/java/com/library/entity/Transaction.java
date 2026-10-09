package com.library.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "transactions")
public class Transaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "fine_amount")
    private Double fineAmount = 0.00;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ISSUED"; // ISSUED, RETURNED, OVERDUE

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;

    public Transaction() {
    }

    public Transaction(User user, Book book, LocalDate issueDate, LocalDate dueDate) {
        this.user = user;
        this.book = book;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.fineAmount = 0.0;
        this.status = "ISSUED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(Double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Compute fine if overdue. Standard default rate is 2.00 currency units/day.
     */
    public double computeCurrentFine(double ratePerDay) {
        LocalDate endPoint = (returnDate != null) ? returnDate : LocalDate.now();
        if (endPoint.isAfter(dueDate)) {
            long overdueDays = ChronoUnit.DAYS.between(dueDate, endPoint);
            return Math.max(0.0, overdueDays * ratePerDay);
        }
        return 0.0;
    }

    public boolean isCurrentlyOverdue() {
        if ("RETURNED".equalsIgnoreCase(status)) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate);
    }
}
