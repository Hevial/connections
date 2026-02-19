package server;

public class Session {

    private String username;

    public Session(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAuthenticated() {
        return username != null && !username.isEmpty();
    }
}
