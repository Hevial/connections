package client.handlers;

import java.util.EnumMap;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.JsonElement;

import client.menus.BaseMenu;
import models.Response;
import models.enums.Action;
import models.enums.StatusCodes;

public class ResponseHandler {

    private final Map<Action, ResponseActionHandler> actionHandlers;

    public ResponseHandler(Scanner scanner) {
        this.actionHandlers = new EnumMap<>(Action.class);
        this.actionHandlers.put(Action.REGISTER, new RegisterResponseHandler());
        this.actionHandlers.put(Action.LOGIN, new LoginResponseHandler(scanner));
        this.actionHandlers.put(Action.UPDATE_CREDENTIALS, new UpdateCredentialsResponseHandler());
        this.actionHandlers.put(Action.LOGOUT, new LogoutResponseHandler(scanner));
        this.actionHandlers.put(Action.GAME_STATUS, new GamestatusResponseHandler());
    }

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
