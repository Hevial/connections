package server.handlers;

import com.google.gson.JsonElement;

import models.Response;
import models.enums.Action;
import models.enums.StatusCodes;
import server.Session;
import server.db.DBManager;

public class LogoutRequestHandler implements RequestActionHandler {

    @Override
    public Response handle(JsonElement data, Session session) {

        DBManager dbManager = DBManager.getInstance();

        if (!session.isAuthenticated()) {
            return new Response(Action.LOGOUT, StatusCodes.UNAUTHORIZED, "Logout fallito: utente non autenticato",
                    null);
        }
        dbManager.logoutUser(session.getUsername());
        session.setUsername(null);
        return new Response(Action.LOGOUT, StatusCodes.SUCCESS, "Logout avvenuto con successo", null);
    }

}
