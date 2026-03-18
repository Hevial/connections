package client.handlers;

import com.google.gson.JsonElement;

import client.menus.BaseMenu;
import models.enums.StatusCodes;

/**
 * Handler for server responses related to user registration.
 */
public class RegisterResponseHandler implements ResponseActionHandler {

    @Override
    public BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu) {
        String msg = statusCode.toString() + " (" + statusCode.getCode() + "), " + message;

        currentMenu.setLastMessage(msg);
        currentMenu.setCurrAction(null);

        return currentMenu;

    }

}
