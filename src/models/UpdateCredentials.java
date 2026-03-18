package models;

/**
 * Request object used to update a user's username.
 */
public class UpdateCredentials {

    String oldUsername;
    String newUsername;

    /**
     * Create an UpdateCredentials request.
     *
     * @param oldUsername current username
     * @param newUsername desired new username
     */
    public UpdateCredentials(String oldUsername, String newUsername) {
        this.oldUsername = oldUsername;
        this.newUsername = newUsername;
    }

    /** @return the current username */
    public String getOldUsername() {
        return oldUsername;
    }

    /** @return the desired new username */
    public String getNewUsername() {
        return newUsername;
    }
}
