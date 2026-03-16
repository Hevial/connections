package client.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import client.CompletedGameFormatter;
import client.menus.BaseMenu;
import models.CompletedGame;
import models.OngoingGameStats;
import models.enums.StatusCodes;

public class GameStatsResponseHandler implements ResponseActionHandler {

    @Override
    public BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu) {
        String msg = statusCode.toString() + " (" + statusCode.getCode() + "), " + message;
        currentMenu.setLastMessage(msg);

        if (statusCode != StatusCodes.SUCCESS) {
            return currentMenu; // Stay on the same menu if there was an error
        }

        // Assuming data contains a CompletedGame object
        Gson gson = new Gson();
        JsonObject dataObj = data.getAsJsonObject();

        if (dataObj.has("OngoingGameStats")) {
            OngoingGameStats stats = gson.fromJson(dataObj.get("OngoingGameStats"), OngoingGameStats.class);
            currentMenu.setGeneralData(stats.toFormattedString());
            currentMenu.showGeneralData();
            return currentMenu;

        }

        if (dataObj.has("CompletedGame")) {
            CompletedGame completedGame = gson.fromJson(dataObj.get("CompletedGame"), CompletedGame.class);
            currentMenu.setGeneralData(CompletedGameFormatter.formatSummary(completedGame));
            currentMenu.showGeneralData();
            return currentMenu;
        }

        return currentMenu; // Stay on the same menu after displaying info
    }

}
