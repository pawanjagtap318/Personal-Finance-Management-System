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
}
