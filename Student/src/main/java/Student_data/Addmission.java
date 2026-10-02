package Student_data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class Addmission extends HttpServlet  {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {

	String id1 = req.getParameter("studentId");
	int id = Integer.parseInt(id1);
	
	String name1 = req.getParameter("studentName");
	
	String email1 = req.getParameter("email");
	
	String course1 = req.getParameter("course");
	
	try {
	Class.forName("com.mysql.cj.jdbc.Driver");
	
	Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/boss","root","root");
	
	PreparedStatement ps = c.prepareStatement("insert into student values (?,?,?,?)");
	
	ps.setInt(1, id);
	ps.setString(2, name1);
	ps.setString(3, email1);
	ps.setString(4, course1);
	
	 int result = ps.executeUpdate();
	
	 if (result > 0) {
		    System.out.println("Success");
		    
		} else {
		    System.out.println("Failed");
		}
	}catch( Exception e) {
		System.out.println(e);
	}
	 
	
	} 
	
}
