package models;

public class Response {
    private Action action;
    private boolean success;
    private String message;
    private Object data;

    public Response(Action action, boolean success, String message, Object data) {
        this.action = action;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public Action getAction() {
        return action;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
