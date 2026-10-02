package EmployeeData;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class Register extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		PrintWriter out = resp.getWriter();
		String empId = req.getParameter("empId");
		int id = Integer.parseInt(empId);

		String empName = req.getParameter("empName");

		String email = req.getParameter("email");

		String salary1 = req.getParameter("salary");
		double salary = Double.parseDouble(salary1);

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/boss", "root", "root");

			String sql = "INSERT INTO employee VALUES (?, ?, ?, ?)";

			PreparedStatement ps = con.prepareStatement(sql);

			ps.setInt(1, id);
			ps.setString(2, empName);
			ps.setString(3, email);
			ps.setDouble(4, salary);

			int check = ps.executeUpdate();
			
			if(check >0) {
				System.out.println("Success");
				out.println("<h1 style='color: green; text-align: center; font-family: Arial;'>✓ Registration Successful!</h1>");
			}else {
				System.out.println("Fail");
				out.println("<h1 style='color: green; text-align: center; font-family: Arial;'>✓ Registration Fail!</h1>");
			}

		} catch (Exception e) {

		}
	}
}