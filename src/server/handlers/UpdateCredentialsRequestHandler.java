package server.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import models.Action;
import models.DBStatus;
import models.Response;
import models.StatusCodes;
import server.db.DBManager;

public class UpdateCredentialsRequestHandler implements RequestActionHandler {

    @Override
    public Response handle(JsonElement data) {
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
        String oldUsername = data.getAsJsonObject().get("oldUser").getAsJsonObject().get("username").getAsString();
        String oldPassword = data.getAsJsonObject().get("oldUser").getAsJsonObject().get("password").getAsString();
        String newUsername = data.getAsJsonObject().get("newUser").getAsJsonObject().get("username").getAsString();
        String newPassword = data.getAsJsonObject().get("newUser").getAsJsonObject().get("password").getAsString();

        // Validate that username and password are not empty
        if (oldUsername.trim().isEmpty() || oldPassword.trim().isEmpty() || newUsername.trim().isEmpty()
                || newPassword.trim().isEmpty()) {
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
