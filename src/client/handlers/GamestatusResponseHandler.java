package client.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import client.CompletedGameFormatter;
import client.PlayerGameStateFormatter;
import client.menus.BaseMenu;
import models.PlayerCompletedGame;
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

        JsonObject dataObj = data.getAsJsonObject();
        if (dataObj.has("playerGameState")) {
            PlayerGameState gameState = gson.fromJson(dataObj.get("playerGameState"),
                    PlayerGameState.class);
            currentMenu.setGameData(PlayerGameStateFormatter.format(gameState));
            currentMenu.showGameData();
        }

        if (dataObj.has("playerCompletedGame")) {
            PlayerCompletedGame gameState = gson.fromJson(dataObj.get("playerCompletedGame"),
                    PlayerCompletedGame.class);
            currentMenu.setGeneralData(CompletedGameFormatter.formatForUser(gameState));
            currentMenu.showGeneralData();
        }

        return currentMenu; // Stay on the same menu after displaying info
    }

}
