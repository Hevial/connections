package models;

public class AuthResponse {

    private String userId;
    private String username;

    public AuthResponse(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

}
