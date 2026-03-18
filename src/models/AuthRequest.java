package models;

/**
 * Simple DTO for authentication requests (login/register).
 *
 * <p>
 * Contains a username and password payload as submitted by the client UI.
 */
public class AuthRequest {

    private String username;
    private String password;

    /**
     * Create a new authentication request.
     *
     * @param username the username
     * @param password the password
     */
    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
