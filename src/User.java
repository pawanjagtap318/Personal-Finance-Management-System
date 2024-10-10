package PersonalFinanceManagementSystem.src;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String password;
    private double salary;
    private double saving;
    private double loanEmi;
    private double sipInvestment;
    private double rent;
    private LocalDateTime registrationDate;
    private String investmentSymbol;

    // Constructor
    public User(int userId, String username, String password, double salary) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.salary = salary;
    }

    // Getters and setters
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public double getSalary() {
        return salary;
    }
    public void setSalary(double salary) {
        this.salary = salary;
    }

    public double getRemainingSalary() {
        return saving;
    }
    public void setRemainingSalary(double saving) {
        this.saving = saving;
    }

    public double getLoanEmi() {
        return loanEmi;
    }
    public void setLoanEmi(double loanEmi) {
        this.loanEmi = loanEmi;
    }

    public double getSipInvestment() {
        return sipInvestment;
    }
    public void setSipInvestment(double sipInvestment) {
        this.sipInvestment = sipInvestment;
    }

    public double getRent() {
        return rent;
    }
    public void setRent(double rent) {
        this.rent = rent;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }
    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }
}
