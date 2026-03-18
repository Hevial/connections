package client.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import client.menus.BaseMenu;
import models.UserStats;
import models.enums.StatusCodes;

/**
 * Handler for responses returning personal statistics for the logged-in user.
 */
public class PersonalStatsResponseHandler implements ResponseActionHandler {

    @Override
    public BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu) {
        String msg = statusCode.toString() + " (" + statusCode.getCode() + "), " + message;
        currentMenu.setLastMessage(msg);

        if (statusCode != StatusCodes.SUCCESS) {
            return currentMenu;
        }

        UserStats stats = new Gson().fromJson(data, UserStats.class);
        currentMenu.setGeneralData(stats.toFormattedString());
        currentMenu.showGeneralData();

        return currentMenu;
    }

}
