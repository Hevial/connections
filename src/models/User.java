package models;

import java.util.UUID;

/**
 * Domain model representing a user account with credentials.
 *
 * <p>The class stores a generated unique identifier ({@code userId}), a
 * username and a password. It is a simple POJO used by authentication and
 * session management code. Passwords are stored in clear-text in this model
 * and should be hashed/secured by the caller before persistent storage in a
 * production system.</p>
 */
public class User {

    /** Stable generated identifier for the user. */
    private String userId;

    /** Chosen username (unique within the system). */
    private String username;

    /** Raw password; callers are responsible for hashing if persistence is used. */
    private String password;

    /**
     * Construct a new user with a generated id.
     *
     * @param username the user's username
     * @param password the user's password (raw form)
     */
    public User(String username, String password) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
    }

    /**
     * Return the username associated with this account.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Update the username for this account.
     *
     * @param username new username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Return the raw password stored in this object.
     * <p>Note: callers should avoid using or transmitting raw passwords;
     * prefer hashed values for persistence and transmission.</p>
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Replace the stored password for this user.
     *
     * @param password the new password (raw)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Return the stable generated user identifier.
     *
     * @return the userId (UUID string)
     */
    public String getUserId() {
        return userId;
    }
}
