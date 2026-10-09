package com.library.servlet;

import com.library.dao.BookDAO;
import com.library.entity.Book;
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

@WebServlet(name = "BookServlet", urlPatterns = {"/api/books/*", "/BookServlet"})
public class BookServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action.toLowerCase()) {
            case "categories":
                List<String> categories = bookDAO.getCategories();
                sendJsonResponse(resp, categories);
                break;

            case "get":
                String idParam = req.getParameter("id");
                if (idParam != null) {
                    try {
                        Long id = Long.parseLong(idParam);
                        Book book = bookDAO.getBookById(id);
                        if (book != null) {
                            sendJsonResponse(resp, book);
                        } else {
                            sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found");
                        }
                    } catch (NumberFormatException e) {
                        sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID");
                    }
                } else {
                    sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing book ID");
                }
                break;

            case "list":
            default:
                String search = req.getParameter("search");
                String category = req.getParameter("category");
                List<Book> books = bookDAO.searchBooks(search, category);
                sendJsonResponse(resp, books);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Enforce admin permission for mutating books
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null || !currentUser.isAdmin()) {
            sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing action parameter");
            return;
        }

        switch (action.toLowerCase()) {
            case "add":
                handleAddBook(req, resp);
                break;
            case "update":
                handleUpdateBook(req, resp);
                break;
            case "delete":
                handleDeleteBook(req, resp);
                break;
            default:
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unsupported action: " + action);
        }
    }

    private void handleAddBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String title = req.getParameter("title");
        String author = req.getParameter("author");
        String isbn = req.getParameter("isbn");
        String category = req.getParameter("category");
        String publisher = req.getParameter("publisher");
        String yearStr = req.getParameter("publicationYear");
        String qtyStr = req.getParameter("quantity");

        if (title == null || title.trim().isEmpty() ||
            author == null || author.trim().isEmpty() ||
            isbn == null || isbn.trim().isEmpty() ||
            category == null || category.trim().isEmpty()) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Title, author, ISBN, and category are required");
            return;
        }

        // Check if ISBN already exists
        if (bookDAO.getBookByIsbn(isbn.trim()) != null) {
            sendJsonError(resp, HttpServletResponse.SC_CONFLICT, "Book with this ISBN already exists");
            return;
        }

        int quantity = 1;
        try {
            if (qtyStr != null && !qtyStr.trim().isEmpty()) {
                quantity = Math.max(1, Integer.parseInt(qtyStr.trim()));
            }
        } catch (NumberFormatException ignored) {}

        Integer year = null;
        try {
            if (yearStr != null && !yearStr.trim().isEmpty()) {
                year = Integer.parseInt(yearStr.trim());
            }
        } catch (NumberFormatException ignored) {}

        Book book = new Book(title.trim(), author.trim(), isbn.trim(), category.trim(), publisher, year, quantity, quantity);
        boolean success = bookDAO.saveBook(book);

        if (success) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Book successfully added to library catalog");
            res.put("book", book);
            sendJsonResponse(resp, res);
        } else {
            sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save book to database");
        }
    }

    private void handleUpdateBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing book ID");
            return;
        }

        try {
            Long id = Long.parseLong(idStr.trim());
            Book book = bookDAO.getBookById(id);
            if (book == null) {
                sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found");
                return;
            }

            String title = req.getParameter("title");
            String author = req.getParameter("author");
            String isbn = req.getParameter("isbn");
            String category = req.getParameter("category");
            String publisher = req.getParameter("publisher");
            String yearStr = req.getParameter("publicationYear");
            String qtyStr = req.getParameter("quantity");

            if (title != null && !title.trim().isEmpty()) book.setTitle(title.trim());
            if (author != null && !author.trim().isEmpty()) book.setAuthor(author.trim());
            if (category != null && !category.trim().isEmpty()) book.setCategory(category.trim());
            if (publisher != null) book.setPublisher(publisher.trim());

            if (isbn != null && !isbn.trim().isEmpty() && !isbn.trim().equals(book.getIsbn())) {
                Book existing = bookDAO.getBookByIsbn(isbn.trim());
                if (existing != null && !existing.getId().equals(id)) {
                    sendJsonError(resp, HttpServletResponse.SC_CONFLICT, "Another book already has this ISBN");
                    return;
                }
                book.setIsbn(isbn.trim());
            }

            if (yearStr != null && !yearStr.trim().isEmpty()) {
                book.setPublicationYear(Integer.parseInt(yearStr.trim()));
            }

            if (qtyStr != null && !qtyStr.trim().isEmpty()) {
                int newQty = Integer.parseInt(qtyStr.trim());
                int diff = newQty - book.getQuantity();
                book.setQuantity(newQty);
                // Adjust available copies proportionately
                book.setAvailableCopies(Math.max(0, book.getAvailableCopies() + diff));
            }

            boolean updated = bookDAO.updateBook(book);
            if (updated) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "Book successfully updated");
                res.put("book", book);
                sendJsonResponse(resp, res);
            } else {
                sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update book");
            }
        } catch (NumberFormatException e) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid number format for book details");
        }
    }

    private void handleDeleteBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing book ID");
            return;
        }

        try {
            Long id = Long.parseLong(idStr.trim());
            boolean deleted = bookDAO.deleteBook(id);
            if (deleted) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "Book deleted successfully");
                sendJsonResponse(resp, res);
            } else {
                sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot delete book (it may have active borrowing transactions)");
            }
        } catch (NumberFormatException e) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID");
        }
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
