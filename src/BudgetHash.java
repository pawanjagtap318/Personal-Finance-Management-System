package PersonalFinanceManagementSystem.src;

import java.util.HashMap;

public class BudgetHash {
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
}
