package PersonalFinanceManagementSystem.src;

import java.util.PriorityQueue;

public class InvestmentHeap {

    class Investment {
        String symbol;
        double marketValue;

        public Investment(String symbol, double marketValue) {
            this.symbol = symbol;
            this.marketValue = marketValue;
        }
    }

    private PriorityQueue<Double> investmentHeaps;

    public InvestmentHeap() {
        investmentHeaps = new PriorityQueue<>();
    }

    public void addInvestment(double amount) {
        investmentHeaps.add(amount);
    }

private PriorityQueue<Investment> investmentHeap = new PriorityQueue<>((a, b) -> Double.compare(b.marketValue, a.marketValue));

    public void showInvestments() {
        System.out.println("Top Investment: " + investmentHeap.peek());
    }

    public void updateInvestmentHeap(String investmentSymbol, double currentMarketValue) {
        Investment investment = new Investment(investmentSymbol, currentMarketValue);
        investmentHeap.add(investment);
    }

    public void displayTopInvestments() {
        for (Investment investment : investmentHeap) {
            System.out.println("Investment: " + investment.symbol + ", Value: " + investment.marketValue);
        }
    }
}
