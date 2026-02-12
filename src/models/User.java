package models;

/**
 * Represents a user with authentication credentials.
 * 
 * This class encapsulates basic user information including a username and
 * password.
 * It provides access to these credentials through getter methods.
 * 
 */
public class User {

    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
