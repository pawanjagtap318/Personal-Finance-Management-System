package PersonalFinanceManagementSystem.src;

import java.util.TreeMap;

public class TransactionTree {
    private TreeMap<Integer, Expense> expenseTree;

    public TransactionTree() {
        expenseTree = new TreeMap<>();
    }

    public void addExpense(Expense expense) {
        expenseTree.put(expense.getExpenseId(), expense);
    }

    public void showExpenses() {
        for (Expense expense : expenseTree.values()) {
            System.out.println("Expense ID: " + expense.getExpenseId() +
                    ", Amount: " + expense.getAmount() +
                    ", Category: " + expense.getCategory());
        }
    }
}
