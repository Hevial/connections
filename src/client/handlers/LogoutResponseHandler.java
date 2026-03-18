package client.handlers;

import com.google.gson.JsonElement;

import client.ClientMain;
import client.menus.BaseMenu;
import client.menus.MainMenu;
import models.enums.StatusCodes;

/**
 * Handler for logout responses. On successful logout the client stops the
 * notification keepalive and transitions back to the main menu.
 */
public class LogoutResponseHandler implements ResponseActionHandler {

    @Override
    public BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu) {
        String msg = statusCode.toString() + " (" + statusCode.getCode() + "), " + message;

        if (statusCode == StatusCodes.SUCCESS) {
            // stop keepalive when logging out
            try {
                ClientMain.stopNotificationKeepalive();
            } catch (Exception ignored) {
            }
            BaseMenu newMenu = new MainMenu();
            newMenu.setLastMessage(msg);
            newMenu.setCurrAction(null);
            return newMenu;
        }

        currentMenu.setLastMessage(msg);
        currentMenu.setCurrAction(null);
        return currentMenu;
    }

}
