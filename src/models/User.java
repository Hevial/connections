package models;

import java.util.UUID;

/**
 * Represents a user with authentication credentials.
 * 
 * This class encapsulates basic user information including a username and
 * password.
 * It provides access to these credentials through getter methods.
 * 
 */
public class User {

    private String userId;
    private String username;
    private String password;

    public User(String username, String password) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
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

    public String getUserId() {
        return userId;
    }
}
