package client.handlers;

import com.google.gson.JsonElement;

import client.menus.BaseMenu;
import models.StatusCodes;

public class UpdateCredentialsResponseHandler implements ResponseActionHandler {

    @Override
    public BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu) {
        String msg = statusCode.toString() + " (" + statusCode.getCode() + "), " + message;

        // update username in the menu if the update was successful and the old username
        // matches the current one
        if (data != null) {
            String newUsername = data.getAsJsonObject().get("newUsr").getAsString();
            String oldUsername = data.getAsJsonObject().get("oldUsr").getAsString();
            if (currentMenu.getUsername() != null && currentMenu.getUsername().equals(oldUsername))
                currentMenu.setUsername(newUsername);
        }
        currentMenu.setLastMessage(msg);
        currentMenu.setCurrAction(null);

        return currentMenu;

    }

}
