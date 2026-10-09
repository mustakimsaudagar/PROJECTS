package com.library.servlet;

import com.library.dao.BookDAO;
import com.library.dao.TransactionDAO;
import com.library.dao.UserDAO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/api/dashboard/stats"})
public class DashboardServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            sendJsonResponse(resp, Map.of("error", "Unauthorized"));
            return;
        }

        Map<String, Object> stats = new HashMap<>();

        if (currentUser.isAdmin()) {
            stats.put("totalTitles", bookDAO.countTotalTitles());
            stats.put("totalCopies", bookDAO.sumTotalCopies());
            stats.put("availableCopies", bookDAO.sumAvailableCopies());
            stats.put("activeLoans", transactionDAO.countActiveLoans());
            stats.put("overdueCount", transactionDAO.countOverdueTransactions());
            stats.put("totalMembers", userDAO.countMembers());
            stats.put("totalFines", transactionDAO.sumTotalFines());
        } else {
            List<Transaction> userTrans = transactionDAO.getTransactionsByUser(currentUser.getId());
            long active = 0;
            long overdue = 0;
            double fines = 0;
            for (Transaction t : userTrans) {
                if (!"RETURNED".equalsIgnoreCase(t.getStatus())) {
                    active++;
                    if (t.isCurrentlyOverdue()) {
                        overdue++;
                    }
                }
                if (t.getFineAmount() != null) {
                    fines += t.getFineAmount();
                }
            }
            stats.put("activeBorrowed", active);
            stats.put("overdueCount", overdue);
            stats.put("totalHistory", userTrans.size());
            stats.put("totalFines", fines);
        }

        sendJsonResponse(resp, stats);
    }

    private void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(JsonUtil.toJson(data));
        out.flush();
    }
}
