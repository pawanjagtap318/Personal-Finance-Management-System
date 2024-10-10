package PersonalFinanceManagementSystem.src;

import java.util.TreeMap;

public class TransactionTree {

    class TransactionNode {
        int userId;
        double transactionAmount;
        String description;
        TransactionNode left, right;

        public TransactionNode(int userId, double amount, String description) {
            this.userId = userId;
            this.transactionAmount = amount;
            this.description = description;
        }
    }

    private TransactionNode root;

    public void addTransaction(int userId, double amount, String description) {
        root = addTransactionRec(root, userId, amount, description);
    }

    private TransactionNode addTransactionRec(TransactionNode node, int userId, double amount, String description) {
        if (node == null) {
            return new TransactionNode(userId, amount, description);
        }

        if (amount < node.transactionAmount) {
            node.left = addTransactionRec(node.left, userId, amount, description);
        } else {
            node.right = addTransactionRec(node.right, userId, amount, description);
        }
        return node;
    }

    public void addMonthlyDeductionTransaction(int userId, double loanEmi, double sipInvestment, double rent) {
        double totalDeduction = loanEmi + sipInvestment + rent;
        addTransaction(userId, totalDeduction, "Monthly Deduction");
    }

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
