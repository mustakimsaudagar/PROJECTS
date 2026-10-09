package com.library.servlet;

import com.library.dao.UserDAO;
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

@WebServlet(name = "UserServlet", urlPatterns = {"/api/users/*"})
public class UserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null || !currentUser.isAdmin()) {
            sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required");
            return;
        }

        String filter = req.getParameter("filter");
        List<User> list;
        if ("students".equalsIgnoreCase(filter)) {
            list = userDAO.getStudents();
        } else {
            list = userDAO.getAllUsers();
        }

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (User u : list) {
            responseList.add(AuthServlet.cleanUserMap(u));
        }

        sendJsonResponse(resp, responseList);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null || !currentUser.isAdmin()) {
            sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required");
            return;
        }

        String action = req.getParameter("action");
        if ("delete".equalsIgnoreCase(action)) {
            String idStr = req.getParameter("id");
            if (idStr != null) {
                try {
                    Long id = Long.parseLong(idStr.trim());
                    if (id.equals(currentUser.getId())) {
                        sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Cannot delete your own active administrator account");
                        return;
                    }
                    boolean ok = userDAO.deleteUser(id);
                    if (ok) {
                        Map<String, Object> res = new HashMap<>();
                        res.put("success", true);
                        res.put("message", "User deleted successfully");
                        sendJsonResponse(resp, res);
                        return;
                    } else {
                        sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot delete user (may have active checkout transactions)");
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
                    return;
                }
            }
        }

        sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
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
