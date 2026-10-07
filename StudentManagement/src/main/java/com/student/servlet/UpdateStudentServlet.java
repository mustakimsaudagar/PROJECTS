package com.student.servlet;

import java.io.IOException;

import com.student.dao.StudentDAO;
import com.student.entity.Student;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/updateStudent")
public class UpdateStudentServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));

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

        student.setId(id);

        StudentDAO dao = new StudentDAO();

        dao.updateStudent(student);

        response.sendRedirect("viewStudents");
    }
}