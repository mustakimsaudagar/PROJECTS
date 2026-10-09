package com.library.servlet;

import com.library.dao.UserDAO;
import com.library.entity.User;
import com.library.util.JsonUtil;
import com.library.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth/*", "/LoginServlet", "/LogoutServlet"})
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String servletPath = req.getServletPath();

        if ("/LogoutServlet".equals(servletPath) || (pathInfo != null && pathInfo.contains("logout"))) {
            handleLogout(req, resp);
            return;
        }

        if (pathInfo != null && pathInfo.contains("current")) {
            handleCurrentUser(req, resp);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String servletPath = req.getServletPath();
        String action = req.getParameter("action");

        if (action == null && pathInfo != null) {
            if (pathInfo.contains("login")) action = "login";
            else if (pathInfo.contains("register")) action = "register";
            else if (pathInfo.contains("logout")) action = "logout";
        }

        if ("/LoginServlet".equals(servletPath)) {
            action = "login";
        }

        if ("login".equalsIgnoreCase(action)) {
            handleLogin(req, resp);
        } else if ("register".equalsIgnoreCase(action)) {
            handleRegister(req, resp);
        } else if ("logout".equalsIgnoreCase(action)) {
            handleLogout(req, resp);
        } else {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required");
            return;
        }

        User user = userDAO.getUserByUsername(username.trim());
        if (user == null || !PasswordUtil.verifyPassword(password, user.getPassword())) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
            return;
        }

        // Setup session
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);

        String redirectUrl = user.isAdmin() ? "admin-dashboard.html" : "student-dashboard.html";

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Login successful");
        data.put("redirect", redirectUrl);
        data.put("user", cleanUserMap(user));

        sendJsonResponse(resp, data);
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String role = req.getParameter("role");

        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "All required fields must be provided");
            return;
        }

        if (userDAO.getUserByUsername(username.trim()) != null) {
            sendJsonError(resp, HttpServletResponse.SC_CONFLICT, "Username already exists");
            return;
        }

        if (userDAO.getUserByEmail(email.trim()) != null) {
            sendJsonError(resp, HttpServletResponse.SC_CONFLICT, "Email is already registered");
            return;
        }

        // Restrict creation of ADMIN via public registration unless specified or default to STUDENT
        String assignedRole = "STUDENT";
        if ("ADMIN".equalsIgnoreCase(role)) {
            // Check if current user is admin, else default to student
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                User current = (User) session.getAttribute("user");
                if (current.isAdmin()) {
                    assignedRole = "ADMIN";
                }
            }
        }

        String hashedPassword = PasswordUtil.hashPassword(password.trim());
        User newUser = new User(username.trim(), hashedPassword, fullName.trim(), email.trim(), phone, assignedRole);

        boolean saved = userDAO.saveUser(newUser);
        if (!saved) {
            sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to register user");
            return;
        }

        // If not already logged in by an admin creating a user, auto log in
        HttpSession session = req.getSession(false);
        boolean isAdminCreating = (session != null && session.getAttribute("user") != null &&
                ((User) session.getAttribute("user")).isAdmin());

        if (!isAdminCreating) {
            session = req.getSession(true);
            session.setAttribute("user", newUser);
        }

        String redirectUrl = newUser.isAdmin() ? "admin-dashboard.html" : "student-dashboard.html";

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Registration successful");
        data.put("redirect", redirectUrl);
        data.put("user", cleanUserMap(newUser));

        sendJsonResponse(resp, data);
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        String accept = req.getHeader("Accept");
        if (accept != null && accept.contains("text/html")) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("message", "Logged out successfully");
            data.put("redirect", "login.html");
            sendJsonResponse(resp, data);
        }
    }

    private void handleCurrentUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Map<String, Object> data = new HashMap<>();

        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            data.put("loggedIn", true);
            data.put("user", cleanUserMap(user));
        } else {
            data.put("loggedIn", false);
        }

        sendJsonResponse(resp, data);
    }

    public static Map<String, Object> cleanUserMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("fullName", user.getFullName());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("role", user.getRole());
        return map;
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
