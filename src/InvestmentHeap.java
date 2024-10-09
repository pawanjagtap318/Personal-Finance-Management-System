package PersonalFinanceManagementSystem.src;

import java.util.PriorityQueue;

public class InvestmentHeap {
    private PriorityQueue<Double> investmentHeap;

    public InvestmentHeap() {
        investmentHeap = new PriorityQueue<>();
    }

    public void addInvestment(double amount) {
        investmentHeap.add(amount);
    }

    public void showInvestments() {
        System.out.println("Top Investment: " + investmentHeap.peek());
    }
}
