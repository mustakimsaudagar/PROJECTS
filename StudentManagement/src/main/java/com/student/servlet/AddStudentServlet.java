package com.student.servlet;

import java.io.IOException;

import com.student.dao.StudentDAO;
import com.student.entity.Student;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/addStudent")
public class AddStudentServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String course = request.getParameter("course");
        int age = Integer.parseInt(request.getParameter("age"));

        Student student = new Student(
                name,
                email,
                course,
                age
        );

        StudentDAO dao = new StudentDAO();

        dao.saveStudent(student);

        response.sendRedirect("index.html");
    }
}