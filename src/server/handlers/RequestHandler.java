package server.handlers;

import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonElement;

import models.Request;
import models.Response;
import models.enums.Action;
import models.enums.StatusCodes;

public class RequestHandler {
    private final Map<Action, RequestActionHandler> actionHandlers;

    public RequestHandler() {
        actionHandlers = new EnumMap<>(Action.class);
        actionHandlers.put(Action.LOGIN, new LoginRequestHandler());
        actionHandlers.put(Action.REGISTER, new RegisterRequestHandler());
        actionHandlers.put(Action.UPDATE_CREDENTIALS, new UpdateCredentialsRequestHandler());
    }

    public Response handleRequest(Request request) {

        Action action = request.getAction();
        JsonElement data = request.getData();

        RequestActionHandler handler = actionHandlers.get(action);
        if (handler == null) {
            return new Response(action, StatusCodes.NOT_FOUND, "Unknown action: " + action, null);
        }
        return handler.handle(data);
    }
}
