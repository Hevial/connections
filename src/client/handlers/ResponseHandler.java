package client.handlers;

import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonElement;

import client.menus.BaseMenu;
import models.Response;
import models.enums.Action;
import models.enums.StatusCodes;

/**
 * Central dispatcher that routes server {@link models.Response} objects to the
 * corresponding {@link ResponseActionHandler} based on
 * {@link models.enums.Action}.
 *
 * <p>
 * This class maintains an internal {@link java.util.EnumMap} of action handlers
 * and provides a single entry point {@link #handleResponse(Response, BaseMenu)}
 * to preprocess responses (e.g. refresh the current username) and delegate
 * actual response processing to the appropriate handler.
 * </p>
 *
 * <p>
 * Side effects: handlers and this dispatcher may modify the provided
 * {@code currentMenu} (for example updating the username or last message) and
 * will write error information to standard error on unexpected conditions.
 * </p>
 *
 * <p>
 * Threading: this class is not explicitly thread-safe and is intended to be
 * called from the client UI or a single client thread.
 * </p>
 *
 * @see ResponseActionHandler
 * @see models.Response
 * @see models.enums.Action
 */
public class ResponseHandler {

    private final Map<Action, ResponseActionHandler> actionHandlers;

    public ResponseHandler() {
        this.actionHandlers = new EnumMap<>(Action.class);
        this.actionHandlers.put(Action.REGISTER, new RegisterResponseHandler());
        this.actionHandlers.put(Action.LOGIN, new LoginResponseHandler());
        this.actionHandlers.put(Action.UPDATE_CREDENTIALS, new UpdateCredentialsResponseHandler());
        this.actionHandlers.put(Action.LOGOUT, new LogoutResponseHandler());
        this.actionHandlers.put(Action.GAME_STATUS, new GamestatusResponseHandler());
        this.actionHandlers.put(Action.SUBMIT_PROPOSAL, new SubmitProposalResponseHandler());
        this.actionHandlers.put(Action.GAME_STATS, new GameStatsResponseHandler());
        this.actionHandlers.put(Action.PERSONAL_STATS, new PersonalStatsResponseHandler());
        this.actionHandlers.put(Action.LEADERBOARD, new LeaderboardResponseHandler());
    }

    /**
     * Handle and dispatch a server response to the appropriate action handler.
     *
     * <p>
     * Preprocessing performed by this method includes updating the
     * {@code currentMenu}'s username using the response's session username when
     * present, and appending a warning to the message if the username was
     * changed by another client. If the {@code response} is {@code null}, an
     * error message is set on the menu and the same {@code currentMenu} is
     * returned.
     * </p>
     *
     * <p>
     * After preprocessing, the method looks up the handler registered for the
     * response's action and delegates processing. If no handler is found, an
     * error is logged, the menu's current action is cleared, and
     * {@code currentMenu} is returned unchanged.
     * </p>
     *
     * @param response    the server response to process; may be {@code null}
     * @param currentMenu the current UI menu instance that may be updated by
     *                    handlers
     * @return the next {@link BaseMenu} to display; may be the same instance
     */
    public BaseMenu handleResponse(Response response, BaseMenu currentMenu) {
        if (response == null) {
            System.err.println("Received null response");
            currentMenu.setLastMessage("Errore di comunicazione con il server. Riprova.");
            return currentMenu;
        }

        String previousUsername = currentMenu.getUsername();
        Action action = response.getAction();
        StatusCodes statusCode = response.getStatusCode();
        String message = response.getMessage();
        JsonElement data = response.getData();
        String sessionUsername = response.getSessionUsername();
        boolean usernameChangedInBackground = previousUsername != null
                && sessionUsername != null
                && !sessionUsername.isBlank()
                && !previousUsername.equals(sessionUsername)
                && !(action == Action.UPDATE_CREDENTIALS && statusCode == StatusCodes.SUCCESS);

        if (sessionUsername != null && !sessionUsername.isBlank()) {
            currentMenu.setUsername(sessionUsername);
        }

        if (usernameChangedInBackground) {
            message = message + " | Avviso: username aggiornato da un altro client (" + previousUsername + " -> "
                    + sessionUsername + ")";
        }

        ResponseActionHandler handler = actionHandlers.get(action);
        if (handler == null) {
            System.err.println("Unknown action in response: " + action);
            currentMenu.setLastMessage("Handler non trovato per l'azione: " + action);
            currentMenu.setCurrAction(null);
            return currentMenu;
        }

        return handler.handle(statusCode, message, data, currentMenu);
    }
}
