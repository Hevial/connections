package server;

import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonElement;

import models.Action;
import models.Response;
import models.StatusCodes;
import server.handlers.LoginRequestHandler;
import server.handlers.RegisterRequestHandler;
import server.handlers.RequestActionHandler;

public class RequestHandler {
    private final Map<Action, RequestActionHandler> actionHandlers;

    public RequestHandler() {
        actionHandlers = new EnumMap<>(Action.class);
        actionHandlers.put(Action.LOGIN, new LoginRequestHandler());
        actionHandlers.put(Action.REGISTER, new RegisterRequestHandler());
    }

    public Response handleRequest(Action action, JsonElement data) {
        RequestActionHandler handler = actionHandlers.get(action);
        if (handler == null) {
            return new Response(action, StatusCodes.NOT_FOUND.getCode(), "Unknown action: " + action, null);
        }
        return handler.handle(data);
    }
}
