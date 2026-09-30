import java.util.ArrayList;
import java.util.Scanner;

public class LibraryManagementSystem {

    static class Book {
        int id;
        String title;
        String author;
        boolean issued;

        Book(int id, String title, String author) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.issued = false;
        }

        @Override
        public String toString() {
            return "ID: " + id +
                   " | Title: " + title +
                   " | Author: " + author +
                   " | Status: " + (issued ? "Issued" : "Available");
        }
    }

    static ArrayList<Book> books = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);

    // Add Book
    static void addBook() {
        System.out.print("Enter Book ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        // Check duplicate ID
        for (Book book : books) {
            if (book.id == id) {
                System.out.println("Book ID already exists!");
                return;
            }
        }

        System.out.print("Enter Book Title: ");
        String title = sc.nextLine();

        System.out.print("Enter Author Name: ");
        String author = sc.nextLine();

        books.add(new Book(id, title, author));

        System.out.println("Book added successfully!");
    }

    // Remove Book
    static void removeBook() {
        System.out.print("Enter Book ID to remove: ");
        int id = sc.nextInt();

        for (Book book : books) {
            if (book.id == id) {

                if (book.issued) {
                    System.out.println("Cannot remove an issued book!");
                    return;
                }

                books.remove(book);
                System.out.println("Book removed successfully!");
                return;
            }
        }

        System.out.println("Book not found!");
    }

    // Issue Book
    static void issueBook() {
        System.out.print("Enter Book ID to issue: ");
        int id = sc.nextInt();

        for (Book book : books) {
            if (book.id == id) {

                if (book.issued) {
                    System.out.println("Book is already issued!");
                } else {
                    book.issued = true;
                    System.out.println("Book issued successfully!");
                }

                return;
            }
        }

        System.out.println("Book not found!");
    }

    // Return Book
    static void returnBook() {
        System.out.print("Enter Book ID to return: ");
        int id = sc.nextInt();

        for (Book book : books) {
            if (book.id == id) {

                if (!book.issued) {
                    System.out.println("Book was not issued!");
                } else {
                    book.issued = false;
                    System.out.println("Book returned successfully!");
                }

                return;
            }
        }

        System.out.println("Book not found!");
    }

    // Search Book
    static void searchBook() {
        sc.nextLine();

        System.out.print("Enter title or author to search: ");
        String keyword = sc.nextLine().toLowerCase();

        boolean found = false;

        for (Book book : books) {
            if (book.title.toLowerCase().contains(keyword) ||
                book.author.toLowerCase().contains(keyword)) {

                System.out.println(book);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No book found!");
        }
    }

    // Display All Books
    static void displayBooks() {

        if (books.isEmpty()) {
            System.out.println("Library is empty!");
            return;
        }

        System.out.println("\n----- All Books -----");

        for (Book book : books) {
            System.out.println(book);
        }
    }

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Add Book");
            System.out.println("2. Remove Book");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. Search Book");
            System.out.println("6. Display All Books");
            System.out.println("7. Exit");

            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    addBook();
                    break;

                case 2:
                    removeBook();
                    break;

                case 3:
                    issueBook();
                    break;

                case 4:
                    returnBook();
                    break;

                case 5:
                    searchBook();
                    break;

                case 6:
                    displayBooks();
                    break;

                case 7:
                    System.out.println("Thank you!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}