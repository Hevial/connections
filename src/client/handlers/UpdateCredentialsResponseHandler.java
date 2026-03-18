package client.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import client.menus.BaseMenu;
import models.UpdateCredentials;
import models.enums.StatusCodes;

/**
 * Handler for responses to update-credentials requests. Updates the UI message
 * and clears the current action after processing.
 */
public class UpdateCredentialsResponseHandler implements ResponseActionHandler {

    @Override
    public BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu) {
        String msg = statusCode.toString() + " (" + statusCode.getCode() + "), " + message;

        // update username in the menu if the update was successful and the old username
        // matches the current one
        if (statusCode == StatusCodes.SUCCESS && data != null) {

            UpdateCredentials creds = new Gson().fromJson(data, UpdateCredentials.class);

            if (currentMenu.getUsername() != null && currentMenu.getUsername().equals(creds.getOldUsername())) {
                currentMenu.setUsername(creds.getNewUsername());
            }
        }
        currentMenu.setLastMessage(msg);
        currentMenu.setCurrAction(null);

        return currentMenu;

    }

}
