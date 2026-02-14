package models;

import com.google.gson.JsonElement;

public class Request {
    private Action action;
    private JsonElement data;

    public Request(Action action, JsonElement data) {
        this.action = action;
        this.data = data;
    }

    public Action getAction() {
        return action;
    }

    public JsonElement getData() {
        return data;
    }
}
