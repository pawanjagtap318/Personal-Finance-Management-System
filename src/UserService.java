package PersonalFinanceManagementSystem.src;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UserService {
    private TransactionTree transactionTree = new TransactionTree();
    private BudgetHash budgetHash = new BudgetHash();
    private InvestmentHeap investmentHeap = new InvestmentHeap();

    private int currentUserId;

//    Register User
    public void registerUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username: ");
        String username = scanner.nextLine();
        System.out.println("Enter password: ");
        String password = scanner.nextLine();
        System.out.println("Enter your salary: ");
        double salary = scanner.nextDouble();
        saveSalary(salary);

        LocalDateTime registrationDate = LocalDateTime.now();  // Capture current date and time

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password, salary, registration_date, saving) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);  // Get the generated user_id
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setDouble(3, salary);
            stmt.setTimestamp(4, Timestamp.valueOf(registrationDate));
            stmt.setDouble(5, salary);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                currentUserId = generatedKeys.getInt(1);  // Set the current user ID
            }

            System.out.println("User registered successfully. | User ID: " + currentUserId + " | Date: " + registrationDate);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    Login User
    public void loginUser() {
        Scanner scanner = new Scanner(System.in);

        // User login process
        System.out.println("Enter username: ");
        String username = scanner.nextLine();
        System.out.println("Enter password: ");
        String password = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Fetch the user details from the database
            String sql = "SELECT user_id, password, salary, registration_date, saving FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (storedPassword.equals(password)) {
                    currentUserId = rs.getInt("user_id");  // Ensure currentUserId is set
                    double currentSalary = rs.getDouble("salary");
                    double saving = rs.getDouble("saving");
                    Timestamp registrationTimestamp = rs.getTimestamp("registration_date");
                    LocalDate registrationDate = registrationTimestamp.toLocalDateTime().toLocalDate();

                    LocalDate currentDate = LocalDate.now();
                    long monthsPassed = ChronoUnit.MONTHS.between(registrationDate, currentDate);

                    // Calculate the total salary to be added based on how many months passed
                    double totalSalaryToAdd = monthsPassed * currentSalary;

                    System.out.println("Login successful.");

                    if (totalSalaryToAdd > 0) {
                        saving += totalSalaryToAdd;  // Add the accumulated salary to the remaining balance

                        // Update the remaining salary in the database
                        String updateSavingSql = "UPDATE users SET saving = ? WHERE user_id = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateSavingSql);
                        updateStmt.setDouble(1, saving);
                        updateStmt.setInt(2, currentUserId);
                        updateStmt.executeUpdate();

                        System.out.println("Salary added for " + monthsPassed + " months.");
                        System.out.println("Your updated remaining saving is: " + saving);
                    } else {
                        System.out.println("No salary needs to be added yet.");
                        System.out.println("Your remaining saving is: " + saving);
                    }
                } else {
                    System.out.println("Incorrect password.");
                }
            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//     Save Salary
    public void saveSalary(double salary) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET salary = ? WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, salary);
            stmt.setInt(2, currentUserId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    Add Expense
    public void addExpense(double amount) {
        Scanner scanner = new Scanner(System.in);

        // Prompt the user for the expense category
        System.out.println("Enter the expense category: ");
        String category = scanner.nextLine();

        LocalDateTime expenseDate = LocalDateTime.now();  // Capture current date and time

        // 1. Reduce the user's saving by the expense amount
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Fetch the current saving from the database
            String sqlFetchSaving = "SELECT saving FROM users WHERE user_id = ?";
            PreparedStatement stmtFetchSaving = conn.prepareStatement(sqlFetchSaving);
            stmtFetchSaving.setInt(1, currentUserId);
            ResultSet rs = stmtFetchSaving.executeQuery();

            if (rs.next()) {
                double currentSaving = rs.getDouble("saving");

                // Check if the expense exceeds the saving
                if (currentSaving < amount) {
                    System.out.println("Insufficient saving to add this expense.");
                    return;
                }

                // Deduct the expense amount from the saving
                double updatedSaving = currentSaving - amount;

                // 2. Update the user's saving in the database
                String sqlUpdateSaving = "UPDATE users SET saving = ? WHERE user_id = ?";
                PreparedStatement stmtUpdateSaving = conn.prepareStatement(sqlUpdateSaving);
                stmtUpdateSaving.setDouble(1, updatedSaving);
                stmtUpdateSaving.setInt(2, currentUserId);
                stmtUpdateSaving.executeUpdate();

                System.out.println("Expense added successfully." + " | New Saving: " + updatedSaving + " | Date: " + expenseDate);
            } else {
                System.out.println("Error: User not found.");
                return;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Add the expense to the transaction tree and the database
        Expense expense = new Expense(0, amount, category); // Include the category here
        transactionTree.addExpense(expense);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO expenses (amount, category, user_id, expense_date) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, amount);
            stmt.setString(2, category); // Store the category in the database
            stmt.setInt(3, currentUserId);  // Use the correct current user ID
            stmt.setTimestamp(4, Timestamp.valueOf(expenseDate));  // Store the expense date and time
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//     Show Expenses
    public void showExpenses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Fetch all expenses for the current user
            String sql = "SELECT amount, category, expense_date FROM expenses WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUserId);  // Use the current user's ID
            ResultSet rs = stmt.executeQuery();

            // Check if there are any expenses
            if (!rs.isBeforeFirst()) {
                System.out.println("No expenses found for your account.");
                return;
            }

            // Display the expenses
            System.out.println("Your past expenses:");
            while (rs.next()) {
                double amount = rs.getDouble("amount");
                String category = rs.getString("category");
                Timestamp expenseDate = rs.getTimestamp("expense_date");
                System.out.println("Expense: " + amount + " | Category: " + category + " | Date: " + expenseDate);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void showBudget() {
        budgetHash.showBudgets();
    }
}
