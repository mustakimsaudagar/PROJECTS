package com.student.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.student.entity.Student;
import com.student.util.HibernateUtil;

public class StudentDAO {
	
	public Student getStudentById(int id) {

	    try (Session session =
	             HibernateUtil.getSessionFactory().openSession()) {

	        return session.get(Student.class, id);

	    } catch (Exception e) {

	        e.printStackTrace();
	        return null;
	    }
	}

    // CREATE
    public void saveStudent(Student student) {

        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            session.persist(student);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            e.printStackTrace();
        }
    }

    // READ
    public List<Student> getAllStudents() {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            return session
                    .createQuery("from Student", Student.class)
                    .list();

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    // UPDATE
    public void updateStudent(Student student) {

        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            session.merge(student);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            e.printStackTrace();
        }
    }

    // DELETE
    public void deleteStudent(int id) {

        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            Student student = session.get(Student.class, id);

            if (student != null) {
                session.remove(student);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            e.printStackTrace();
        }
        
    }
}