package models;

import com.google.gson.JsonElement;

import models.enums.Action;
import models.enums.StatusCodes;

public class Response {
    private Action action;
    private StatusCodes statusCode;
    private String message;
    private JsonElement data;
    private String sessionUsername;

    public Response(Action action, StatusCodes statusCode, String message, JsonElement data) {
        this.action = action;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        this.sessionUsername = null;
    }

    public Action getAction() {
        return action;
    }

    public StatusCodes getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public JsonElement getData() {
        return data;
    }

    public String getSessionUsername() {
        return sessionUsername;
    }

    public void setSessionUsername(String sessionUsername) {
        this.sessionUsername = sessionUsername;
    }
}
