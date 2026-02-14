package client;

import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonElement;

import client.handlers.RegisterResponseHandler;
import client.handlers.ResponseActionHandler;
import models.Action;
import models.Response;
import models.StatusCodes;

public class ResponseHandler {

    private final Map<Action, ResponseActionHandler> actionHandlers;

    public ResponseHandler() {
        this.actionHandlers = new EnumMap<>(Action.class);
        this.actionHandlers.put(Action.REGISTER, new RegisterResponseHandler());
    }

    public void handleResponse(Response response) {
        if (response == null) {
            System.err.println("Received null response");
            return;
        }

        Action action = response.getAction();
        StatusCodes statusCode = response.getStatusCode();
        String message = response.getMessage();
        JsonElement data = response.getData();

        ResponseActionHandler handler = actionHandlers.get(action);
        if (handler == null) {
            System.err.println("Unknown action in response: " + action);
            return;
        }
        handler.handle(statusCode, message, data);
    }
}
