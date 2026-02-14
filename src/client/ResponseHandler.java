package client;

import java.util.Map;

import com.google.gson.JsonElement;

import client.handlers.ResponseActionHandler;
import models.Action;
import models.StatusCodes;

public class ResponseHandler {

    private final Map<Action, ResponseActionHandler> actionHandlers = Map.of(
    // Action.LOGIN, new LoginResponseHandler(),
    // Action.LOGOUT, new LogoutResponseHandler(),
    // Action.START_GAME, new StartGameHandler(),
    // ...
    );

    public void handleResponse(Action action, StatusCodes statusCode, String message, JsonElement data) {
        ResponseActionHandler handler = actionHandlers.get(action);
        if (handler == null) {
            System.err.println("Unknown action in response: " + action);
            return;
        }
        handler.handle(statusCode, message, data);
    }
}
