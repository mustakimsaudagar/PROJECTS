package com.library.servlet;

import com.library.dao.TransactionDAO;
import com.library.entity.Transaction;
import com.library.entity.User;
import com.library.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "TransactionServlet", urlPatterns = {"/api/transactions/*", "/TransactionServlet"})
public class TransactionServlet extends HttpServlet {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to view transactions");
            return;
        }

        List<Transaction> transactions;
        if (currentUser.isAdmin()) {
            String filter = req.getParameter("filter");
            String userIdParam = req.getParameter("userId");

            if (userIdParam != null && !userIdParam.trim().isEmpty()) {
                try {
                    Long userId = Long.parseLong(userIdParam.trim());
                    transactions = transactionDAO.getTransactionsByUser(userId);
                } catch (NumberFormatException e) {
                    transactions = transactionDAO.getAllTransactions();
                }
            } else if ("active".equalsIgnoreCase(filter)) {
                transactions = transactionDAO.getActiveTransactions();
            } else {
                transactions = transactionDAO.getAllTransactions();
            }
        } else {
            // Student can only see their own transactions
            transactions = transactionDAO.getTransactionsByUser(currentUser.getId());
        }

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Transaction t : transactions) {
            responseList.add(formatTransactionMap(t));
        }

        sendJsonResponse(resp, responseList);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to perform this action");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing action parameter");
            return;
        }

        switch (action.toLowerCase()) {
            case "issue":
                handleIssueBook(req, resp, currentUser);
                break;
            case "borrow":
                handleStudentBorrow(req, resp, currentUser);
                break;
            case "return":
                handleReturnBook(req, resp, currentUser);
                break;
            default:
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action: " + action);
        }
    }

    private void handleIssueBook(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        if (!currentUser.isAdmin()) {
            sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Only library staff/administrators can issue books to arbitrary users");
            return;
        }

        String userIdStr = req.getParameter("userId");
        String bookIdStr = req.getParameter("bookId");
        String daysStr = req.getParameter("days");
        String remarks = req.getParameter("remarks");

        if (userIdStr == null || bookIdStr == null) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "User ID and Book ID are required");
            return;
        }

        try {
            Long userId = Long.parseLong(userIdStr.trim());
            Long bookId = Long.parseLong(bookIdStr.trim());
            int days = 14;
            if (daysStr != null && !daysStr.trim().isEmpty()) {
                days = Integer.parseInt(daysStr.trim());
            }

            boolean ok = transactionDAO.issueBook(userId, bookId, days, remarks);
            if (ok) {
                Map<String, Object> data = new HashMap<>();
                data.put("success", true);
                data.put("message", "Book successfully issued");
                sendJsonResponse(resp, data);
            } else {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Could not issue book. Check if book has available copies or if user/book exists.");
            }
        } catch (NumberFormatException e) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid User or Book ID format");
        }
    }

    private void handleStudentBorrow(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        String bookIdStr = req.getParameter("bookId");
        String daysStr = req.getParameter("days");

        if (bookIdStr == null) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Book ID is required");
            return;
        }

        try {
            Long bookId = Long.parseLong(bookIdStr.trim());
            int days = 14;
            if (daysStr != null && !daysStr.trim().isEmpty()) {
                days = Integer.parseInt(daysStr.trim());
            }

            boolean ok = transactionDAO.issueBook(currentUser.getId(), bookId, days, "Self-service online checkout");
            if (ok) {
                Map<String, Object> data = new HashMap<>();
                data.put("success", true);
                data.put("message", "Book successfully borrowed! Due in " + days + " days.");
                sendJsonResponse(resp, data);
            } else {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Cannot borrow: No available copies remaining for this title.");
            }
        } catch (NumberFormatException e) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid Book ID");
        }
    }

    private void handleReturnBook(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        String txIdStr = req.getParameter("transactionId");
        String fineStr = req.getParameter("finePaid");
        String remarks = req.getParameter("remarks");

        if (txIdStr == null) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Transaction ID is required");
            return;
        }

        try {
            Long txId = Long.parseLong(txIdStr.trim());
            Transaction tx = transactionDAO.getTransactionById(txId);
            if (tx == null) {
                sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Transaction not found");
                return;
            }

            // Students can only return their own books; Admins can return anyone's
            if (!currentUser.isAdmin() && !tx.getUser().getId().equals(currentUser.getId())) {
                sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "You cannot return a transaction belonging to another user");
                return;
            }

            Double fine = null;
            if (fineStr != null && !fineStr.trim().isEmpty()) {
                fine = Double.parseDouble(fineStr.trim());
            }

            boolean ok = transactionDAO.returnBook(txId, fine, remarks);
            if (ok) {
                Map<String, Object> data = new HashMap<>();
                data.put("success", true);
                data.put("message", "Book returned successfully. Stock restored.");
                sendJsonResponse(resp, data);
            } else {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Failed to process book return. It may already be returned.");
            }
        } catch (NumberFormatException e) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid number format for Transaction ID or Fine");
        }
    }

    private Map<String, Object> formatTransactionMap(Transaction t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("userId", t.getUser().getId());
        m.put("userName", t.getUser().getFullName());
        m.put("userEmail", t.getUser().getEmail());
        m.put("userUsername", t.getUser().getUsername());
        m.put("bookId", t.getBook().getId());
        m.put("bookTitle", t.getBook().getTitle());
        m.put("bookAuthor", t.getBook().getAuthor());
        m.put("bookIsbn", t.getBook().getIsbn());
        m.put("issueDate", t.getIssueDate() != null ? t.getIssueDate().toString() : "");
        m.put("dueDate", t.getDueDate() != null ? t.getDueDate().toString() : "");
        m.put("returnDate", t.getReturnDate() != null ? t.getReturnDate().toString() : null);
        m.put("fineAmount", t.getFineAmount() != null ? t.getFineAmount() : 0.0);
        m.put("status", t.getStatus());
        m.put("remarks", t.getRemarks());
        m.put("isOverdue", t.isCurrentlyOverdue());
        return m;
    }

    private void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(JsonUtil.toJson(data));
        out.flush();
    }

    private void sendJsonError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> err = new HashMap<>();
        err.put("success", false);
        err.put("message", message);
        PrintWriter out = resp.getWriter();
        out.print(JsonUtil.toJson(err));
        out.flush();
    }
}
