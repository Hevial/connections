package client.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import client.PlayerGameStateFormatter;
import client.menus.BaseMenu;
import models.PlayerGameState;
import models.enums.StatusCodes;

public class GamestatusResponseHandler implements ResponseActionHandler {

    @Override
    public BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu) {
        String msg = statusCode.toString() + " (" + statusCode.getCode() + "), " + message;
        currentMenu.setLastMessage(msg);

        if (statusCode != StatusCodes.SUCCESS) {
            return currentMenu; // Stay on the same menu if there was an error
        }

        Gson gson = new Gson();
        PlayerGameState gameState = gson.fromJson(data.getAsJsonObject().get("playerGameState"), PlayerGameState.class);
        currentMenu.setData(PlayerGameStateFormatter.format(gameState));
        currentMenu.showGameData();

        return currentMenu; // Stay on the same menu after displaying info
    }

}
