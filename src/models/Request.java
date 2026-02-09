package models;

public class Request {
    private Action action;
    private Object data;

    public Request(Action action, Object data) {
        this.action = action;
        this.data = data;
    }

    public Action getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }
}
