package server.handlers;

import com.google.gson.JsonElement;

import models.Response;
import models.enums.Action;
import models.enums.StatusCodes;
import server.NotificationRegistry;
import server.Session;
import server.db.DBManager;

/**
 * Handler for {@code LOGOUT} requests.
 *
 * <p>
 * Performs session logout by removing the user from the logged-in set in
 * {@link server.db.DBManager}, unregistering any notification address from
 * {@link server.NotificationRegistry}, and clearing session state. If the
 * session is not authenticated the handler returns an
 * {@link StatusCodes#UNAUTHORIZED}
 * response.
 * </p>
 */
public class LogoutRequestHandler implements RequestActionHandler {

    @Override
    public Response handle(JsonElement data, Session session) {

        DBManager dbManager = DBManager.getInstance();

        if (!session.isAuthenticated()) {
            return new Response(Action.LOGOUT, StatusCodes.UNAUTHORIZED, "Logout fallito: utente non autenticato",
                    null);
        }

        dbManager.logoutUser(session.getUserId());
        // unregister notification address
        NotificationRegistry.unregister(session.getUserId());

        session.setUsername(null);
        session.setUserId(null);
        return new Response(Action.LOGOUT, StatusCodes.SUCCESS, "Logout avvenuto con successo", null);
    }

}
