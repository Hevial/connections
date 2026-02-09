package client.handlers;

import com.google.gson.JsonElement;

public interface ResponseActionHandler {
    void handle(boolean success, String message, JsonElement data);
}
