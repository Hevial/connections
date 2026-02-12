package models;

public class Response {
    private Action action;
    private int statusCode;
    private String message;
    private Object data;

    public Response(Action action, int statusCode, String message, Object data) {
        this.action = action;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public Action getAction() {
        return action;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
