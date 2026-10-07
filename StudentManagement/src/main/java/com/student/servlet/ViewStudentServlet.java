package com.student.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.student.dao.StudentDAO;
import com.student.entity.Student;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/viewStudents")
public class ViewStudentServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		StudentDAO dao = new StudentDAO();

		List<Student> students = dao.getAllStudents();

		response.setContentType("text/html");

		PrintWriter out = response.getWriter();

		out.println("<html>");
		out.println("<head>");

		out.println("<title>View Students</title>");

		out.println("<link rel='stylesheet' href='css/style.css'>");

		out.println("</head>");

		out.println("<body>");

		out.println("<div class='table-container'>");

		out.println("<h1>Student List</h1>");

		out.println("<table class='student-table'>");

		out.println("<tr>");

		out.println("<th>ID</th>");
		out.println("<th>Name</th>");
		out.println("<th>Email</th>");
		out.println("<th>Course</th>");
		out.println("<th>Age</th>");
		out.println("<th>Action</th>");

		out.println("</tr>");

		for (Student student : students) {

			out.println("<tr>");

			out.println("<td>" + student.getId() + "</td>");

			out.println("<td>" + student.getName() + "</td>");

			out.println("<td>" + student.getEmail() + "</td>");

			out.println("<td>" + student.getCourse() + "</td>");

			out.println("<td>" + student.getAge() + "</td>");

			out.println("<td>");

			out.println("<a class='edit-btn' href='editStudent?id=" + student.getId() + "'>Edit</a>");

			out.println(" ");

			out.println("<a class='delete-btn' href='deleteStudent?id=" + student.getId() + "' "
					+ "onclick=\"return confirm('Are you sure you want to delete this student?');\">" + "Delete</a>");

			out.println("</td>");

			out.println("</tr>");
		}

		out.println("</table>");

		out.println("<div class='page-buttons'>");

		out.println("<a class='btn' href='add-student.html'>Add Student</a>");

		out.println("<a class='btn' href='index.html'>Home</a>");

		out.println("</div>");

		out.println("</div>");

		out.println("</body>");

		out.println("</html>");
	}
}