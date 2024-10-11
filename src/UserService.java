package PersonalFinanceManagementSystem.src;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.sql.Date;
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

    FixedExpenses fixedExpenses = new FixedExpenses();

    public int currentUserId;
    public double saving;
    public double loanEmi;
    public double rent;
    public double sipInvestment;

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
        // Set up monthly expenses: Loan EMI, Investment (SIP), Rent
        System.out.println("Set your Loan EMI: ");
        loanEmi = scanner.nextDouble();
        System.out.println("Set your monthly Rent: ");
        rent = scanner.nextDouble();
        System.out.println("Set your monthly Investment (SIP): ");
        sipInvestment = scanner.nextDouble();
//        System.out.println("Enter Investment Symbol for tracking: ");
//        String investmentSymbol = scanner.next();

        LocalDateTime registrationDate = LocalDateTime.now();  // Capture current date and time
        LocalDate today = LocalDate.now();

        try (Connection conn = DatabaseConnection.getConnection()) {
//            String sql = "INSERT INTO users (username, password, salary, registration_date, saving, loan_emi, sip_investment, rent, investment_symbol) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sql = "INSERT INTO users (username, password, salary, registration_date, saving, loan_emi, sip_invst, rent, emi_srt_dt, sip_srt_dt, rent_srt_dt) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);  // Get the generated user_id
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setDouble(3, salary);
            stmt.setTimestamp(4, Timestamp.valueOf(registrationDate));
            stmt.setDouble(5, salary);
            stmt.setDouble(6, loanEmi);
            stmt.setDouble(7, sipInvestment);
            stmt.setDouble(8, rent);
            stmt.setDate(9, Date.valueOf(today)); // EMI start date
            stmt.setDate(10, Date.valueOf(today)); // SIP start date
            stmt.setDate(11, Date.valueOf(today)); // Rent start date
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                currentUserId = generatedKeys.getInt(1);  // Set the current user ID
            }

            System.out.println("User registered successfully. | User ID: " + currentUserId + " | Date: " + registrationDate);
            fixedExpenses.deductInitialDeductions(currentUserId, loanEmi, sipInvestment, rent);
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
                    saving = rs.getDouble("saving");
                    Timestamp registrationTimestamp = rs.getTimestamp("registration_date");
                    LocalDate registrationDate = registrationTimestamp.toLocalDateTime().toLocalDate();

                    LocalDate currentDate = LocalDate.now();
                    long monthsPassed = ChronoUnit.MONTHS.between(registrationDate, currentDate);

                    // Calculate the total salary to be added based on how many months passed
                    double totalSalaryToAdd = monthsPassed * currentSalary;

                    System.out.println("Login successful.");

                    if (totalSalaryToAdd > 0) {
                        saving += totalSalaryToAdd;  // Add the accumulated salary to the saving

                        // Update the saving in the database
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

                    monthlyExpenses();

                } else {
                    System.out.println("Incorrect password.");
                    System.exit(0);
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
                saving = rs.getDouble("saving");

                // Check if the expense exceeds the saving
                if (saving < amount) {
                    System.out.println("Insufficient saving to add this expense.");
                    return;
                }

                // Deduct the expense amount from the saving
                saving -= amount;

                // 2. Update the user's saving in the database
                String sqlUpdateSaving = "UPDATE users SET saving = ? WHERE user_id = ?";
                PreparedStatement stmtUpdateSaving = conn.prepareStatement(sqlUpdateSaving);
                stmtUpdateSaving.setDouble(1, saving);
                stmtUpdateSaving.setInt(2, currentUserId);
                stmtUpdateSaving.executeUpdate();

                System.out.println("Expense added successfully." + " | Saving: " + saving + " | Date: " + expenseDate);
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

    public void showSaving(){
        try {
            Connection conn = DatabaseConnection.getConnection();
            // Fetch the current saving from the database
            String sqlFetchSaving = "SELECT saving FROM users WHERE user_id = ?";
            PreparedStatement stmtFetchSaving = conn.prepareStatement(sqlFetchSaving);
            stmtFetchSaving.setInt(1, currentUserId);
            ResultSet rs = stmtFetchSaving.executeQuery();

            if (rs.next()) {
                double saving = rs.getDouble("saving");
                System.out.println("Saving:" + saving);
            }else {
                System.out.println("Error: User not found.");
                return;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showBudget() {
        budgetHash.showBudgets();
    }

    public void showBudgetBreakdown() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Scanner scanner = new Scanner(System.in);

            String sql = "SELECT loan_emi, sip_invst, rent FROM users WHERE user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, currentUserId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                loanEmi = resultSet.getDouble("loan_emi");
                sipInvestment = resultSet.getDouble("sip_invst");
                rent = resultSet.getDouble("rent");

                System.out.println("Budget Breakdown for User ID: " + currentUserId);
                System.out.println("Loan EMI: " + loanEmi);
                System.out.println("SIP Investment: " + sipInvestment);
                System.out.println("Monthly Rent: " + rent);

            } else {
                System.out.println("User not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Simulate tracking of investment growth
    public void trackInvestmentGrowth() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            // Fetch user's SIP investment details
            String query = "SELECT sip_invst, sip_srt_dt FROM users WHERE user_id = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, currentUserId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double initialInvestment = resultSet.getDouble("sip_invst");
                LocalDate sipStartDate = resultSet.getDate("sip_srt_dt").toLocalDate();
                LocalDate currentDate = LocalDate.now();

                // Calculate the number of months since SIP started
                long monthsElapsed = ChronoUnit.MONTHS.between(sipStartDate, currentDate);

                if (monthsElapsed > 0 && initialInvestment > 0) {
                    // Assume a fixed annual growth rate (5%) or 0.417% monthly growth
                    double monthlyGrowthRate = 0.00417; // 5% annual growth rate
                    double growthMultiplier = Math.pow(1 + monthlyGrowthRate, monthsElapsed);
                    double currentInvestmentValue = initialInvestment * growthMultiplier;

                    System.out.println("SIP started on: " + sipStartDate);
                    System.out.println("Months elapsed: " + monthsElapsed);
                    System.out.println("Initial Investment: ₹" + initialInvestment);
                    System.out.println("Current Investment Value: ₹" + currentInvestmentValue);
                }
                else if (monthsElapsed == 0 && initialInvestment > 0) {
                    // Assume a fixed annual growth rate (5%) or 0.417% monthly growth
                    double monthlyGrowthRate = 0.00417; // 5% annual growth rate
                    double growthMultiplier = Math.pow(1 + monthlyGrowthRate, monthsElapsed);
                    double currentInvestmentValue = initialInvestment * growthMultiplier;

                    System.out.println("SIP started on: " + sipStartDate);
                    System.out.println("Months elapsed: " + monthsElapsed);
                    System.out.println("Initial Investment: ₹" + initialInvestment);
                    System.out.println("Current Investment Value: ₹" + currentInvestmentValue);
                }
                else {
                    System.out.println("No valid SIP investment found.");
                }
            } else {
                System.out.println("User not found or no SIP investment data available.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void monthlyExpenses(){
        fixedExpenses.applyEmiDeduction(currentUserId);
        fixedExpenses.applySipDeduction(currentUserId);
        fixedExpenses.applyRentDeduction(currentUserId);
    }
}