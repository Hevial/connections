package models;

public class UpdateCredentials {

    String oldUsername;
    String newUsername;

    public UpdateCredentials(String oldUsername, String newUsername) {
        this.oldUsername = oldUsername;
        this.newUsername = newUsername;
    }

    public String getOldUsername() {
        return oldUsername;
    }

    public String getNewUsername() {
        return newUsername;
    }
}
