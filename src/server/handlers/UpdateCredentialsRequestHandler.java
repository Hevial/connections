package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import models.Response;
import models.User;
import models.enums.Action;
import models.enums.StatusCodes;
import server.Session;
import server.db.DBManager;
import server.db.DBStatus;

public class UpdateCredentialsRequestHandler implements RequestActionHandler {

    @Override
    public Response handle(JsonElement data, Session session) {
        // Validate request body
        if (data == null || !data.isJsonObject()) {
            return new Response(Action.UPDATE_CREDENTIALS, StatusCodes.BAD_REQUEST,
                    "Registration failed: Invalid request body", null);
        }

        // Validate required fields
        JsonObject body = data.getAsJsonObject();
        if (!body.has("oldUser") || !body.has("newUser")) {
            return new Response(Action.UPDATE_CREDENTIALS, StatusCodes.BAD_REQUEST,
                    "Registration failed: Missing credentials", null);
        }

        // Estrarre i dati dal JsonElement
        Gson gson = new Gson();
        User oldUser = gson.fromJson(body.get("oldUser"), User.class);
        User newUser = gson.fromJson(body.get("newUser"), User.class);
        String oldUsername = oldUser.getUsername().trim();
        String oldPassword = oldUser.getPassword();
        String newUsername = newUser.getUsername().trim();
        String newPassword = newUser.getPassword();
        // Validate that username and password are not empty
        if (oldUsername.isEmpty() || oldPassword.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()) {
            return new Response(Action.UPDATE_CREDENTIALS, StatusCodes.BAD_REQUEST,
                    "Errore: username e password non possono essere vuoti", null);
        }

        DBManager dbManager = DBManager.getInstance();

        try {
            DBStatus status = dbManager.updateCredentials(oldUsername, oldPassword, newUsername, newPassword);

            switch (status) {
                case SUCCESS:
                    JsonElement resData = new JsonObject();
                    resData.getAsJsonObject().addProperty("newUsr", newUsername);
                    resData.getAsJsonObject().addProperty("oldUsr", oldUsername);
                    if (session.isAuthenticated() && session.getUsername().equals(oldUsername)) {
                        session.setUsername(newUsername);
                    }
                    return new Response(Action.UPDATE_CREDENTIALS, StatusCodes.SUCCESS,
                            "Credenziali aggiornate con successo", resData);
                case USER_NOT_FOUND:
                    return new Response(Action.UPDATE_CREDENTIALS, StatusCodes.NOT_FOUND,
                            "Utente non trovato", null);
                case WRONG_PASSWORD:
                    return new Response(Action.UPDATE_CREDENTIALS, StatusCodes.UNAUTHORIZED,
                            "Credenziali non valide", null);
                case USERNAME_ALREADY_EXISTS:
                    return new Response(Action.UPDATE_CREDENTIALS, StatusCodes.CONFLICT,
                            "Il nuovo username è già in uso", null);
                default:
                    return new Response(Action.UPDATE_CREDENTIALS, StatusCodes.INTERNAL_SERVER_ERROR,
                            "Errore del database durante l'aggiornamento delle credenziali", null);
            }
        } catch (Exception e) {
            return new Response(Action.UPDATE_CREDENTIALS, StatusCodes.INTERNAL_SERVER_ERROR,
                    "Update credentials failed: Server Error", null);
        }
    }
}