package com.library.dao;

import com.library.entity.Book;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

public class BookDAO {

    public boolean saveBook(Book book) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(book);
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

    public boolean updateBook(Book book) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(book);
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

    public boolean deleteBook(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Book book = session.get(Book.class, id);
            if (book != null) {
                session.remove(book);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public Book getBookById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Book.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Book getBookByIsbn(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery("FROM Book WHERE isbn = :isbn", Book.class);
            query.setParameter("isbn", isbn);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Book> getAllBooks() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery("FROM Book ORDER BY id DESC", Book.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Book> searchBooks(String searchTerm, String category) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Book WHERE 1=1 ");

            boolean hasSearch = searchTerm != null && !searchTerm.trim().isEmpty();
            boolean hasCategory = category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category);

            if (hasSearch) {
                hql.append("AND (LOWER(title) LIKE :search OR LOWER(author) LIKE :search OR isbn LIKE :search) ");
            }
            if (hasCategory) {
                hql.append("AND LOWER(category) = LOWER(:category) ");
            }
            hql.append("ORDER BY title ASC");

            Query<Book> query = session.createQuery(hql.toString(), Book.class);
            if (hasSearch) {
                query.setParameter("search", "%" + searchTerm.trim().toLowerCase() + "%");
            }
            if (hasCategory) {
                query.setParameter("category", category.trim());
            }
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> getCategories() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<String> query = session.createQuery("SELECT DISTINCT category FROM Book ORDER BY category ASC", String.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public long countTotalTitles() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(b) FROM Book b", Long.class);
            return query.uniqueResult() != null ? query.uniqueResult() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long sumTotalCopies() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COALESCE(SUM(b.quantity), 0) FROM Book b", Long.class);
            return query.uniqueResult() != null ? query.uniqueResult() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long sumAvailableCopies() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COALESCE(SUM(b.availableCopies), 0) FROM Book b", Long.class);
            return query.uniqueResult() != null ? query.uniqueResult() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
