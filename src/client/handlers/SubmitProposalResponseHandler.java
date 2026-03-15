package client.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import client.PlayerGameStateFormatter;
import client.menus.BaseMenu;
import models.PlayerGameState;
import models.enums.StatusCodes;

public class SubmitProposalResponseHandler implements ResponseActionHandler {

    @Override
    public BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu) {

        currentMenu.hideGameData();
        currentMenu.setCurrAction(null);

        try {
            if (data != null && data.isJsonObject() && data.getAsJsonObject().has("playerGameState")) {
                PlayerGameState playerGameState = new Gson().fromJson(data.getAsJsonObject().get("playerGameState"),
                        PlayerGameState.class);
                currentMenu.setData(PlayerGameStateFormatter.format(playerGameState));
            }
        } catch (Exception e) {
            currentMenu
                    .setLastMessage(message + "; Impossibile aggiornare lo stato partita dalla risposta del server.");
            return currentMenu;
        }

        if (statusCode != StatusCodes.SUCCESS) {
            currentMenu.setLastMessage(statusCode.toString() + " (" + statusCode.getCode() + "), " + message);
            return currentMenu;
        }

        currentMenu.setLastMessage(message);
        return currentMenu;
    }

}
