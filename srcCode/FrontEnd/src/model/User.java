package model;

public class User {
    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String dateCreated;

    // Constructor
    public User(Integer id, String username, String fullName, String email, String role, String dateCreated) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.dateCreated = dateCreated;
    }

    // Getters
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getDateCreated() { return dateCreated; }
}
