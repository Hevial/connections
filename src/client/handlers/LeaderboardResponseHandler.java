package client.handlers;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import client.LeaderboardFormatter;
import client.menus.BaseMenu;
import models.LeaderboardEntry;
import models.enums.StatusCodes;

public class LeaderboardResponseHandler implements ResponseActionHandler {

    @Override
    public BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu) {
        String msg = statusCode.toString() + " (" + statusCode.getCode() + "), " + message;
        currentMenu.setLastMessage(msg);

        if (statusCode != StatusCodes.SUCCESS) {
            return currentMenu;
        }

        Type listType = new TypeToken<List<LeaderboardEntry>>() {
        }.getType();
        List<LeaderboardEntry> leaderboardEntries = new Gson().fromJson(data, listType);

        String formattedLeaderboard = LeaderboardFormatter.format(leaderboardEntries);

        currentMenu.setGeneralData(formattedLeaderboard);
        currentMenu.showGeneralData();

        return currentMenu;
    }

}