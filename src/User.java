package PersonalFinanceManagementSystem.src;

public class User {
    private int userId;
    private String username;
    private String password;
    private double salary;

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
}
