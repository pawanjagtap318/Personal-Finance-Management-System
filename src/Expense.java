package PersonalFinanceManagementSystem.src;

public class Expense {
    private int expenseId;
    private double amount;
    private String category;

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
}
