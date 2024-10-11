package PersonalFinanceManagementSystem.src;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Scanner;

public class BudgetHash {

    private double lEmi;
    private double rnt;
    private double sipInvst;

    private HashMap<String, Double> budgetHash;

    public BudgetHash() {
        budgetHash = new HashMap<>();
    }

    public void allocateBudget(String category, double amount) {
        budgetHash.put(category, amount);
    }

    public void showBudgets() {
        for (String category : budgetHash.keySet()) {
            System.out.println("Category: " + category + ", Budget: " + budgetHash.get(category));
        }
    }

    public void hashBudgetCategories(User user) {
        budgetHash.put("Loan EMI", user.getLoanEmi());
        budgetHash.put("SIP Investment", user.getSipInvestment());
        budgetHash.put("Rent", user.getRent());
    }

    public void displayBudgetCategories() {
        for (String key : budgetHash.keySet()) {
            System.out.println(key + ": " + budgetHash.get(key));
        }
    }

    public void editBudget(int userId) {
        Scanner scanner = new Scanner(System.in);

        double lEmi = -1, rnt = -1, sipInvst = -1;

        // Get user input for each category with the option to stop (set to 0) or leave unchanged (-1)
        System.out.println("Set your Loan EMI (to stop, enter 0; for unchanged, enter -1): ");
        lEmi = scanner.nextDouble();

        System.out.println("Set your monthly Rent (to stop, enter 0; for unchanged, enter -1): ");
        rnt = scanner.nextDouble();

        System.out.println("Set your monthly Investment (SIP) (to stop, enter 0; for unchanged, enter -1): ");
        sipInvst = scanner.nextDouble();

        // Update the database with the new values
        budgetUpdate(userId, lEmi, rnt, sipInvst);
    }

    public void budgetUpdate(int userId, double lEmi, double rnt, double sipInvst) {
        try {
            Connection conn = DatabaseConnection.getConnection();

            // Query to get the current savings of the user
            String sql = "SELECT saving, loan_emi, emi_srt_dt, last_emi_ded, sip_invst, sip_srt_dt, last_sip_ded, rent, rent_srt_dt, last_rent_ded FROM users WHERE user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                double saving = resultSet.getDouble("saving");
                double loanEmi = resultSet.getDouble("loan_emi");
                Date emiStartDate = resultSet.getDate("emi_srt_dt");
                Date lastEmiDeduction = resultSet.getDate("last_emi_ded");
                double sipInvestment = resultSet.getDouble("sip_invst");
                Date sipStartDate = resultSet.getDate("sip_srt_dt");
                Date lastSipDeduction = resultSet.getDate("last_sip_ded");
                double rent = resultSet.getDouble("rent");
                Date rentStartDate = resultSet.getDate("rent_srt_dt");
                Date lastRentDeduction = resultSet.getDate("last_rent_ded");

                // Calculate the total amount that will be deducted based on user inputs
                double newLoanEmi = (lEmi == -1) ? loanEmi : lEmi;
                double newSipInvestment = (sipInvst == -1) ? sipInvestment : sipInvst;
                double newRent = (rnt == -1) ? rent : rnt;

                double totalRequiredDeduction = newLoanEmi + newSipInvestment + newRent;

                // Check if savings are sufficient
                if (saving < totalRequiredDeduction) {
                    System.out.println("Error: Not enough savings to apply the updated budget. Current savings: ₹" + saving);
                    return; // Do not update the budget
                }

                // Update the user's savings and budget categories in the database
                String updateSql = "UPDATE users SET saving = ?, loan_emi = ?, emi_srt_dt = ?, last_emi_ded = ?, sip_invst = ?, sip_srt_dt = ?, last_sip_ded = ?, rent = ?, rent_srt_dt = ?, last_rent_ded = ? WHERE user_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);

                // If Loan EMI is updated
                if (lEmi != -1) {
                    if (lEmi == 0) {
                        updateStmt.setDouble(2, 0); // Set EMI to 0
                        updateStmt.setNull(3, java.sql.Types.DATE); // Reset EMI start date
                        updateStmt.setNull(4, java.sql.Types.DATE); // Reset last EMI deduction
                    } else {
                        updateStmt.setDouble(2, lEmi); // Set new EMI value
                        if (emiStartDate == null) {
                            updateStmt.setDate(3, Date.valueOf(LocalDate.now())); // Set new EMI start date if none exists
                        } else {
                            updateStmt.setDate(3, emiStartDate); // Keep old EMI start date
                        }
                        updateStmt.setDate(4, Date.valueOf(LocalDate.now())); // Update last EMI deduction date
                        saving -= lEmi; // Deduct EMI from savings
                    }
                } else {
                    updateStmt.setDouble(2, loanEmi); // Keep old EMI
                    updateStmt.setDate(3, emiStartDate);
                    updateStmt.setDate(4, lastEmiDeduction);
                }

                // If SIP Investment is updated
                if (sipInvst != -1) {
                    if (sipInvst == 0) {
                        updateStmt.setDouble(5, 0); // Set SIP to 0
                        updateStmt.setNull(6, java.sql.Types.DATE); // Reset SIP start date
                        updateStmt.setNull(7, java.sql.Types.DATE); // Reset last SIP deduction
                    } else {
                        updateStmt.setDouble(5, sipInvst); // Set new SIP value
                        if (sipStartDate == null) {
                            updateStmt.setDate(6, Date.valueOf(LocalDate.now())); // Set new SIP start date if none exists
                        } else {
                            updateStmt.setDate(6, sipStartDate); // Keep old SIP start date
                        }
                        updateStmt.setDate(7, Date.valueOf(LocalDate.now())); // Update last SIP deduction date
                        saving -= sipInvst; // Deduct SIP from savings
                    }
                } else {
                    updateStmt.setDouble(5, sipInvestment); // Keep old SIP Investment
                    updateStmt.setDate(6, sipStartDate); // Keep old SIP start date
                    updateStmt.setDate(7, lastSipDeduction); // Keep old last SIP deduction date
                }

                // If Rent is updated
                if (rnt != -1) {
                    if (rnt == 0) {
                        updateStmt.setDouble(8, 0); // Set Rent to 0
                        updateStmt.setNull(9, java.sql.Types.DATE); // Reset Rent start date
                        updateStmt.setNull(10, java.sql.Types.DATE); // Reset last Rent deduction
                    } else {
                        updateStmt.setDouble(8, rnt); // Set new Rent value
                        if (rentStartDate == null) {
                            updateStmt.setDate(9, Date.valueOf(LocalDate.now())); // Set new Rent start date if none exists
                        } else {
                            updateStmt.setDate(9, rentStartDate); // Keep old Rent start date
                        }
                        updateStmt.setDate(10, Date.valueOf(LocalDate.now())); // Update last Rent deduction date
                        saving -= rnt; // Deduct Rent from savings
                    }
                } else {
                    updateStmt.setDouble(8, rent); // Keep old Rent
                    updateStmt.setDate(9, rentStartDate);
                    updateStmt.setDate(10, lastRentDeduction);
                }

                // Update user's savings
                updateStmt.setDouble(1, saving);
                updateStmt.setInt(11, userId);
                updateStmt.executeUpdate();

                System.out.println("Budget updated successfully.");
                System.out.println("Updated Savings: ₹" + saving);

            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
