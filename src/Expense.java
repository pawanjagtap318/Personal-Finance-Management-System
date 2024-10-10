package PersonalFinanceManagementSystem.src;

import java.time.LocalDateTime;

public class Expense {
    private int userId;
    private int expenseId;
    private double amount;
    private String category;
    private LocalDateTime expenseDate;

    // Constructor
    public Expense(int expenseId, double amount, String category) {
        this.expenseId = expenseId;
        this.amount = amount;
        this.category = category;
    }

    // Getters and setters
    public int getExpenseId() {
        return expenseId;
    }
    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getExpenseDate() {
        return expenseDate;
    }
    public void setExpenseDate(LocalDateTime expenseDate) {
        this.expenseDate = expenseDate;
    }
}
