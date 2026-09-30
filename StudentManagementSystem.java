package collection_frameworks;

import java.util.ArrayList;
//import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class StudentManagementSystem {

	public class Student {

		private int rollno;
		private String name;
		private String standard;

		public Student(int rollno, String name, String standard) {
			this.rollno = rollno;
			this.name = name;
			this.standard = standard;
		}

		public int getRollno() {
			return rollno;
		}

		public void setRollno(int rollno) {
			this.rollno = rollno;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getStandard() {
			return standard;
		}

		public void setStandard(String standard) {
			this.standard = standard;
		}

		@Override
		public String toString() {
			return "Student [rollno=" + rollno + ", name=" + name + ", standard=" + standard + "]";
		}

	}

//	private static final Student student = null;

	Map<Integer, Student> map = new HashMap<>();
	Student s1 = new Student(1, "Ram", "1st");
	Student s2 = new Student(2, "Sham", "2st");
	Student s3 = new Student(3, "Sachin", "3st");
	Student s4 = new Student(4, "Pratik", "4st");

	public void putVal() {
		map.put(s1.getRollno(), s1);
		map.put(s2.getRollno(), s2);
		map.put(s3.getRollno(), s3);
		map.put(s4.getRollno(), s4);
	}

	public List<Student> display() {
		return new ArrayList<Student>(map.values());
	}

	public Student setStudent(int roll, String name, String standard) {
		if (map.containsKey(roll)) {
			return null;
		}

		Student student = new Student(roll, name, standard);
		map.put(roll, student);

		return student;
	}

	public Student deleteStudent(int rollno) {
		return map.remove(rollno);

	}

	public Student updateStudent(int rollno, String name, String standard) {

		if (!map.containsKey(rollno)) {
			return null;
		}

		Student student = new Student(rollno, name, standard);
		map.put(rollno, student);

		return student;
	}

	public Student searchStudent(int rollno) {
		return map.get(rollno);
	}

	public boolean existStudent(int rollno) {
		return map.containsKey(rollno);
	}

	public int countStudent() {
		return map.size();
	}

	public List<Student> displayByStandard(String standard) {
		List<Student> students = new ArrayList<>();
		for (Student s : map.values()) {
			if (s.getStandard().equals(standard)) {
				students.add(s);
			}
		}
		return students;
	}

	public static void main(String[] args) {

		StudentManagementSystem re = new StudentManagementSystem();

		re.putVal();

		Scanner sc = new Scanner(System.in);
		boolean s = true;
		while (s) {

			System.out.println();
			System.out.println("=".repeat(10) + " Student management system " + "=".repeat(10));
			System.out.println("Enter your choice");
			System.out.println("Enter 1 for add student");
			System.out.println("Enter 2 for update student");
			System.out.println("Enter 3 for delete student");
			System.out.println("Enter 4 for search student");
			System.out.println("Enter 5 for Check student exist");
			System.out.println("Enter 6 for Count students ");
			System.out.println("Enter 7 for Display all students");
			System.out.println("Enter 8 for Exit");
			int r = sc.nextInt();

			switch (r) {

			case 1:
				System.out.println("=".repeat(10) + " Add student " + "=".repeat(10));
				System.out.println("Enter roll no");
				int roll = sc.nextInt();
				sc.nextLine();
				System.out.println("Enter Name");
				String name = sc.nextLine();
				System.out.println("Enter Standard");
				String standard = sc.nextLine();
				Student add = re.setStudent(roll, name, standard);
				if (add == null) {
					System.out.println("Enter different roll no");
				} else {
					System.out.println("Added student " + add);
				}
				break;

			case 2:
				System.out.println("=".repeat(10) + " Update student " + "=".repeat(10));
				System.out.println("Enter Existing roll no");
				int roll1 = sc.nextInt();
				sc.nextLine();
				System.out.println("Enter Name");
				String name1 = sc.nextLine();
				System.out.println("Enter Standard");
				String standard1 = sc.nextLine();
				Student updated = re.updateStudent(roll1, name1, standard1);
				if (updated == null) {
					System.out.println("Student not found");
				} else {
					System.out.println("Student Updated " + updated);
				}
				break;

			case 3:
				System.out.println("=".repeat(10) + " Delete student " + "=".repeat(10));
				System.out.println("Enter roll no to delete");
				int rollno = sc.nextInt();
				System.out.println("Student deleted " + re.deleteStudent(rollno));
				break;

			case 4:
				System.out.println("=".repeat(10) + " Search student " + "=".repeat(10));
				System.out.println("Enter rollno to search student");
				int rollno1 = sc.nextInt();
				Student student = re.searchStudent(rollno1);
				if (student == null) {
					System.out.println("Student not found");
				} else {
					System.out.println("Student = " + student);
				}
				break;

			case 5:
				System.out.println("=".repeat(10) + " Check exist student " + "=".repeat(10));
				System.out.println("Enter rollno to check if student exist");
				int rollno2 = sc.nextInt();
				System.out.println("Is student exist? " + re.existStudent(rollno2));
				break;

			case 6:
				System.out.println("=".repeat(10) + " Count student " + "=".repeat(10));
				System.out.println("Total count of student is " + re.countStudent());
				break;

			case 7:
				System.out.println("=".repeat(10) + " Display all students " + "=".repeat(10));
				for (Student s1 : re.display()) {
					System.out.println(s1);
				}
				break;

			case 8:
				System.out.println("=".repeat(10) + " Thankyou for using our system " + "=".repeat(10));
				s = false;
				break;

			}
		}
		sc.close();
	}
}
