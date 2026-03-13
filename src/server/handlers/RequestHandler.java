package server.handlers;

import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonElement;

import models.Request;
import models.Response;
import models.User;
import models.enums.Action;
import models.enums.StatusCodes;
import server.GameManager;
import server.Session;
import server.db.DBManager;

public class RequestHandler {
    private final Map<Action, RequestActionHandler> actionHandlers;

    public RequestHandler(GameManager gameManager) {
        actionHandlers = new EnumMap<>(Action.class);
        actionHandlers.put(Action.LOGIN, new LoginRequestHandler(gameManager));
        actionHandlers.put(Action.GAME_STATUS, new GamestatusRequestHandler(gameManager));
        actionHandlers.put(Action.REGISTER, new RegisterRequestHandler());
        actionHandlers.put(Action.UPDATE_CREDENTIALS, new UpdateCredentialsRequestHandler());
        actionHandlers.put(Action.LOGOUT, new LogoutRequestHandler());
    }

    public Response handleRequest(Request request, Session session) {

        if (session.getUserId() != null) {
            User currentUser = DBManager.getInstance().getUserById(session.getUserId());
            if (currentUser != null) {
                session.setUsername(currentUser.getUsername());
            } else {
                session.setUserId(null);
                session.setUsername(null);
            }
        }

        Action action = request.getAction();
        JsonElement data = request.getData();

        RequestActionHandler handler = actionHandlers.get(action);
        if (handler == null) {
            return new Response(action, StatusCodes.NOT_FOUND, "Unknown action: " + action, null);
        }
        return handler.handle(data, session);
    }
}
