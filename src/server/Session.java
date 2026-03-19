package server;

/**
 * Represents a per-connection session that holds authentication state for a
 * connected client.
 *
 * <p>The {@code Session} stores the authenticated {@code userId} and
 * corresponding {@code username}. It is created for each connection handled by
 * {@link server.ServerWorker} and passed to request handlers to allow them to
 * determine the acting user.</p>
 */
public class Session {

    private String userId;
    private String username;

    /**
     * Create a session initialized with the provided identifiers.
     *
     * @param userId   optional user id (may be null for unauthenticated sessions)
     * @param username optional username (may be null)
     */
    public Session(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    /**
     * Return the session username, or {@code null} if unauthenticated.
     *
     * @return the username or {@code null}
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the session username. Callers should also update {@link #userId}
     * consistently when changing the authenticated user.
     *
     * @param username new username or {@code null} to clear
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Check whether the session is authenticated.
     *
     * @return {@code true} when a non-empty username is present
     */
    public boolean isAuthenticated() {
        return username != null && !username.isEmpty();
    }

    /**
     * Return the authenticated user id, or {@code null} if unauthenticated.
     *
     * @return the userId or {@code null}
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the authenticated user id associated with this session.
     *
     * @param userId new user id or {@code null} to clear
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
