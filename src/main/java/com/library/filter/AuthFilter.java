package com.library.filter;

import com.library.entity.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"*.html", "/"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // If visiting root /
        if (uri.endsWith("/") || uri.endsWith("/index.html")) {
            if (user != null) {
                resp.sendRedirect(req.getContextPath() + (user.isAdmin() ? "/admin-dashboard.html" : "/student-dashboard.html"));
                return;
            }
        }

        // If visiting login or register while already logged in
        if (uri.endsWith("/login.html") || uri.endsWith("/register.html")) {
            if (user != null) {
                resp.sendRedirect(req.getContextPath() + (user.isAdmin() ? "/admin-dashboard.html" : "/student-dashboard.html"));
                return;
            }
        }

        // Protect admin dashboard
        if (uri.endsWith("/admin-dashboard.html")) {
            if (user == null) {
                resp.sendRedirect(req.getContextPath() + "/login.html");
                return;
            }
            if (!user.isAdmin()) {
                resp.sendRedirect(req.getContextPath() + "/student-dashboard.html");
                return;
            }
        }

        // Protect student dashboard
        if (uri.endsWith("/student-dashboard.html")) {
            if (user == null) {
                resp.sendRedirect(req.getContextPath() + "/login.html");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
