package com.student.test;

import com.student.dao.StudentDAO;
import com.student.entity.Student;

public class TestHibernate {

    public static void main(String[] args) {

        Student student = new Student(
                "Rahul",
                "rahul@gmail.com",
                "Java",
                22
        );

        StudentDAO dao = new StudentDAO();

        dao.saveStudent(student);

        System.out.println("Student saved successfully!");
    }
}