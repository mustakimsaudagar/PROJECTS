package com.student.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import com.student.dao.StudentDAO;
import com.student.entity.Student;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/editStudent")
public class EditStudentServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));

        StudentDAO dao = new StudentDAO();

        Student student = dao.getStudentById(id);

        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Edit Student</title>");

        out.println("<style>");

        out.println("body {");
        out.println("font-family: Arial;");
        out.println("background-color: #f4f4f4;");
        out.println("}");

        out.println(".container {");
        out.println("width: 400px;");
        out.println("margin: 80px auto;");
        out.println("background: white;");
        out.println("padding: 30px;");
        out.println("border-radius: 10px;");
        out.println("box-shadow: 0 0 10px #ccc;");
        out.println("}");

        out.println("input {");
        out.println("width: 100%;");
        out.println("padding: 10px;");
        out.println("margin: 8px 0 15px 0;");
        out.println("box-sizing: border-box;");
        out.println("}");

        out.println("button {");
        out.println("width: 100%;");
        out.println("padding: 12px;");
        out.println("background: #333;");
        out.println("color: white;");
        out.println("border: none;");
        out.println("cursor: pointer;");
        out.println("}");

        out.println("</style>");
        out.println("</head>");

        out.println("<body>");

        out.println("<div class='container'>");

        out.println("<h2>Edit Student</h2>");

        out.println("<form action='updateStudent' method='post'>");

        out.println("<input type='hidden' name='id' value='"
                + student.getId() + "'>");

        out.println("<label>Name</label>");

        out.println("<input type='text' name='name' value='"
                + student.getName() + "' required>");

        out.println("<label>Email</label>");

        out.println("<input type='email' name='email' value='"
                + student.getEmail() + "' required>");

        out.println("<label>Course</label>");

        out.println("<input type='text' name='course' value='"
                + student.getCourse() + "' required>");

        out.println("<label>Age</label>");

        out.println("<input type='number' name='age' value='"
                + student.getAge() + "' required>");

        out.println("<button type='submit'>Update Student</button>");

        out.println("</form>");

        out.println("</div>");

        out.println("</body>");
        out.println("</html>");
    }
}