package server;

import java.util.Map;

import com.google.gson.JsonElement;

import models.Action;
import models.Response;
import models.statusCodes;
import server.handlers.LoginRequestHandler;
import server.handlers.RegisterRequestHandler;
import server.handlers.RequestActionHandler;

public class RequestHandler {
    private final Map<Action, RequestActionHandler> actionHandlers = Map.of(
            Action.LOGIN, new LoginRequestHandler(),
            Action.REGISTER, new RegisterRequestHandler());

    public Response handleRequest(Action action, JsonElement data) {
        RequestActionHandler handler = actionHandlers.get(action);
        if (handler == null) {
            return new Response(action, statusCodes.NOT_FOUND.getCode(), "Unknown action: " + action, null);
        }
        return handler.handle(data);
    }
}
