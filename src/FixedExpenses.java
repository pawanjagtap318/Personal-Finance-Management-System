package PersonalFinanceManagementSystem.src;

import java.time.LocalDate;
import java.sql.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Period;

public class FixedExpenses {

    public void deductInitialDeductions(int userId, double loanEmi, double sipInvestment, double rent) {
        try {
            Connection conn = DatabaseConnection.getConnection();

            // Query to get the current savings of the user
            String sql = "SELECT saving FROM users WHERE user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                double saving = resultSet.getDouble("saving");

                // Calculate total deductions
                double totalDeductions = loanEmi + sipInvestment + rent;

                // Deduct the total from savings
                saving -= totalDeductions;
                System.out.println("Initial deductions applied: ₹" + totalDeductions);
                System.out.println("New savings after deductions: ₹" + saving);

                // Update the user's savings in the database
                String updateSql = "UPDATE users SET saving = ?, last_emi_ded = ?, last_sip_ded = ?, last_rent_ded = ? WHERE user_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setDouble(1, saving);
                updateStmt.setDate(2, Date.valueOf(LocalDate.now())); // Set last deduction dates to today
                updateStmt.setDate(3, Date.valueOf(LocalDate.now()));
                updateStmt.setDate(4, Date.valueOf(LocalDate.now()));
                updateStmt.setInt(5, userId);
                updateStmt.executeUpdate();

                System.out.println("Savings updated successfully.");
                System.out.println("Saving: " + saving);

            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void applyEmiDeduction(int userId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT saving, loan_emi, emi_srt_dt, last_emi_ded FROM users WHERE user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                double saving = resultSet.getDouble("saving");
                double loanEmi = resultSet.getDouble("loan_emi");
                Date emiStartDate = resultSet.getDate("emi_srt_dt");
                Date lastEmiDeduction = resultSet.getDate("last_emi_ded");

                LocalDate currentDate = LocalDate.now();
                LocalDate lastDeductionDate = (lastEmiDeduction == null) ? emiStartDate.toLocalDate() : lastEmiDeduction.toLocalDate();

                // Ensure the EMI is not deducted multiple times in the same month
                if (loanEmi > 0 && emiStartDate != null && currentDate.isAfter(lastDeductionDate)) {
                    Period periodSinceLastDeduction = Period.between(lastDeductionDate, currentDate);

                    // Check if it's a new month since the last EMI deduction
                    if (periodSinceLastDeduction.getMonths() > 0 || periodSinceLastDeduction.getYears() > 0) {
                        // Calculate missed months (if any)
                        int monthsMissed = Period.between(lastDeductionDate, currentDate).getMonths();
                        monthsMissed += (currentDate.getYear() - lastDeductionDate.getYear()) * 12;

                        if (currentDate.getDayOfMonth() >= emiStartDate.toLocalDate().getDayOfMonth()) {
                            monthsMissed++;  // Include the current month
                        }

                        if (monthsMissed > 0) {
                            // Deduct EMI for each missed month
                            double totalEmiDeduction = loanEmi * monthsMissed;
                            saving -= totalEmiDeduction;

                            System.out.println("EMI deducted for " + monthsMissed + " months: ₹" + totalEmiDeduction);

                            // Update the saving and last EMI deduction date
                            String updateSql = "UPDATE users SET saving = ?, last_emi_ded = ? WHERE user_id = ?";
                            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                            updateStmt.setDouble(1, saving);
                            updateStmt.setDate(2, Date.valueOf(currentDate));  // Update to today's date
                            updateStmt.setInt(3, userId);
                            updateStmt.executeUpdate();

                            System.out.println("Updated EMI deductions. New saving: ₹" + saving);
                        } else {
                            System.out.println("No EMI deduction required for today.");
                        }
                    } else {
                        System.out.println("EMI already deducted for this month.");
                    }
                } else if (loanEmi == 0 || emiStartDate == null) {
                    System.out.println("No EMI is set.");
                } else {
                    System.out.println("No EMI deduction required for today.");
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void applySipDeduction(int userId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT saving, sip_invst, sip_srt_dt, last_sip_ded FROM users WHERE user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                double saving = resultSet.getDouble("saving");
                double sipInvestment = resultSet.getDouble("sip_invst");
                Date sipStartDate = resultSet.getDate("sip_srt_dt");
                Date lastSipDeduction = resultSet.getDate("last_sip_ded");

                LocalDate currentDate = LocalDate.now();
                LocalDate lastDeductionDate = (lastSipDeduction == null) ? sipStartDate.toLocalDate() : lastSipDeduction.toLocalDate();

                // Ensure the SIP is not deducted multiple times in the same month
                if (sipInvestment > 0 && sipStartDate != null && currentDate.isAfter(lastDeductionDate)) {
                    Period periodSinceLastDeduction = Period.between(lastDeductionDate, currentDate);

                    // Check if it's a new month since the last SIP deduction
                    if (periodSinceLastDeduction.getMonths() > 0 || periodSinceLastDeduction.getYears() > 0) {
                        // Calculate missed months (if any)
                        int monthsMissed = Period.between(lastDeductionDate, currentDate).getMonths();
                        monthsMissed += (currentDate.getYear() - lastDeductionDate.getYear()) * 12;

                        if (currentDate.getDayOfMonth() >= sipStartDate.toLocalDate().getDayOfMonth()) {
                            monthsMissed++;  // Include the current month
                        }

                        if (monthsMissed > 0) {
                            // Deduct SIP for each missed month
                            double totalSipDeduction = sipInvestment * monthsMissed;
                            saving -= totalSipDeduction;

                            System.out.println("SIP deducted for " + monthsMissed + " months: ₹" + totalSipDeduction);

                            // Update the saving and last SIP deduction date
                            String updateSql = "UPDATE users SET saving = ?, last_sip_ded = ? WHERE user_id = ?";
                            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                            updateStmt.setDouble(1, saving);
                            updateStmt.setDate(2, Date.valueOf(currentDate));  // Update to today's date
                            updateStmt.setInt(3, userId);
                            updateStmt.executeUpdate();

                            System.out.println("Updated SIP deductions. New saving: ₹" + saving);
                        } else {
                            System.out.println("No SIP deduction required for today.");
                        }
                    } else {
                        System.out.println("SIP already deducted for this month.");
                    }
                } else if (sipInvestment == 0 || sipStartDate == null) {
                    System.out.println("No SIP is set.");
                } else {
                    System.out.println("No SIP deduction required for today.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void applyRentDeduction(int userId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT saving, rent, rent_srt_dt, last_rent_ded FROM users WHERE user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                double saving = resultSet.getDouble("saving");
                double rent = resultSet.getDouble("rent");
                Date rentStartDate = resultSet.getDate("rent_srt_dt");
                Date lastRentDeduction = resultSet.getDate("last_rent_ded");

                LocalDate currentDate = LocalDate.now();
                LocalDate lastDeductionDate = (lastRentDeduction == null) ? rentStartDate.toLocalDate() : lastRentDeduction.toLocalDate();

                // Ensure the rent is not deducted multiple times in the same month
                if (rent > 0 && rentStartDate != null && currentDate.isAfter(lastDeductionDate)) {
                    Period periodSinceLastDeduction = Period.between(lastDeductionDate, currentDate);

                    // Check if it's a new month since the last rent deduction
                    if (periodSinceLastDeduction.getMonths() > 0 || periodSinceLastDeduction.getYears() > 0) {
                        // Calculate missed months (if any)
                        int monthsMissed = Period.between(lastDeductionDate, currentDate).getMonths();
                        monthsMissed += (currentDate.getYear() - lastDeductionDate.getYear()) * 12;

                        if (currentDate.getDayOfMonth() >= rentStartDate.toLocalDate().getDayOfMonth()) {
                            monthsMissed++;  // Include the current month
                        }

                        if (monthsMissed > 0) {
                            // Deduct rent for each missed month
                            double totalRentDeduction = rent * monthsMissed;
                            saving -= totalRentDeduction;

                            System.out.println("Rent deducted for " + monthsMissed + " months: ₹" + totalRentDeduction);

                            // Update the saving and last rent deduction date
                            String updateSql = "UPDATE users SET saving = ?, last_rent_ded = ? WHERE user_id = ?";
                            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                            updateStmt.setDouble(1, saving);
                            updateStmt.setDate(2, Date.valueOf(currentDate));  // Update to today's date
                            updateStmt.setInt(3, userId);
                            updateStmt.executeUpdate();

                            System.out.println("Updated Rent deductions. New saving: ₹" + saving);
                        } else {
                            System.out.println("No Rent deduction required for today.");
                        }
                    } else {
                        System.out.println("Rent already deducted for this month.");
                    }
                } else if (rent == 0 || rentStartDate == null) {
                    System.out.println("No Rent is set.");
                } else {
                    System.out.println("No Rent deduction required for today.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
