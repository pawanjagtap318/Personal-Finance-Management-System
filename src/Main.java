package PersonalFinanceManagementSystem.src;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();

        Scanner scanner = new Scanner(System.in);

        System.out.println("\nWelcome to Personal Finance Management System");
        System.out.println("1. Register");
        System.out.println("2. Login");

        int choice = scanner.nextInt();

        if (choice == 1) {
            userService.registerUser();
        } else if (choice == 2) {
            userService.loginUser();
        }

        while (true) {
            System.out.println("\n1. Add Expense");
            System.out.println("2. Show Expenses");
            System.out.println("3. Show Saving");
            System.out.println("4. Show Budget");
            System.out.println("5. Exit");

            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    System.out.println("Enter expense amount: ");
                    double amount = scanner.nextDouble();
                    userService.addExpense(amount);
                    break;
                case 2:
                    userService.showExpenses();  // Show all expenses for the logged-in user
                    break;
                case 3:
                    userService.showSaving();  // Show current saving amount
                    break;
                case 4:
                    userService.showBudget();
                    break;
                case 5:
                    System.exit(0);
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

}
