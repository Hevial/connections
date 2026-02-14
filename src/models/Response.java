package models;

import com.google.gson.JsonElement;

public class Response {
    private Action action;
    private StatusCodes statusCode;
    private String message;
    private JsonElement data;

    public Response(Action action, StatusCodes statusCode, String message, JsonElement data) {
        this.action = action;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
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
}
