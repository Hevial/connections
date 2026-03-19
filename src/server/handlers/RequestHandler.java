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

/**
 * Central dispatcher that maps {@link models.enums.Action} values to their
 * corresponding {@link RequestActionHandler} implementations.
 *
 * <p>This class encapsulates the registration of all action handlers and
 * provides a single entry point {@link #handleRequest(Request, Session)} to
 * process incoming requests: it resolves the appropriate handler and delegates
 * execution, performing light session reconciliation before dispatch.</p>
 */
public class RequestHandler {
    private final Map<Action, RequestActionHandler> actionHandlers;

    public RequestHandler(GameManager gameManager) {
        actionHandlers = new EnumMap<>(Action.class);
        actionHandlers.put(Action.LOGIN, new LoginRequestHandler(gameManager));
        actionHandlers.put(Action.GAME_STATUS, new GamestatusRequestHandler(gameManager));
        actionHandlers.put(Action.REGISTER, new RegisterRequestHandler());
        actionHandlers.put(Action.UPDATE_CREDENTIALS, new UpdateCredentialsRequestHandler());
        actionHandlers.put(Action.LOGOUT, new LogoutRequestHandler());
        actionHandlers.put(Action.SUBMIT_PROPOSAL, new SubmitProposalRequestHandler(gameManager));
        actionHandlers.put(Action.GAME_STATS, new GameStatsRequestHandler(gameManager));
        actionHandlers.put(Action.PERSONAL_STATS, new PersonalStatsRequestHandler());
        actionHandlers.put(Action.LEADERBOARD, new LeaderboardRequestHandler());
    }

    /**
     * Handle an incoming request by delegating to the registered action handler.
     *
     * <p>The method performs light session reconciliation: if the session has a
     * {@code userId} it attempts to refresh the username from the database and
     * clears the session if the user is no longer present.</p>
     *
     * @param request incoming {@link Request} to handle
     * @param session the {@link Session} representing the connected client
     * @return a {@link Response} produced by the corresponding action handler
     */
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
