package server;

import java.util.Map;

import com.google.gson.JsonElement;

import models.Action;
import models.Response;
import server.handlers.RequestActionHandler;

public class RequestHandler {
    private final Map<Action, RequestActionHandler> actionHandlers = Map.of(
    // Action.LOGIN, new LoginRequestHandler(),
    // Action.LOGOUT, new LogoutRequestHandler(),
    // Action.START_GAME, new StartGameRequestHandler(),
    // ...
    );

    public Response handleRequest(Action action, JsonElement data, String userId) {
        RequestActionHandler handler = actionHandlers.get(action);
        if (handler == null) {
            return new Response(action, false, "Unknown action: " + action, null);
        }
        return handler.handle(data, userId);
    }
}
