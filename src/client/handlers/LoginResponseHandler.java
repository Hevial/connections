package client.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import client.ClientMain;
import client.PlayerGameStateFormatter;
import client.menus.BaseMenu;
import client.menus.UserMenu;
import models.PlayerGameState;
import models.enums.StatusCodes;

/**
 * Handler for login responses. On success transitions the UI to
 * {@link client.menus.UserMenu} and initializes per-player data. On failure
 * it leaves the current menu unchanged and shows the error message.
 */
public class LoginResponseHandler implements ResponseActionHandler {

    @Override
    public BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu) {
        String msg = statusCode.toString() + " (" + statusCode.getCode() + "), " + message;

        // If login failed, clear any stored username and remain in the current menu
        if (statusCode != StatusCodes.SUCCESS) {
            currentMenu.setUsername(null);
            currentMenu.setLastMessage(msg);
            currentMenu.setCurrAction(null);
            return currentMenu;
        }

        // Successful login: build UserMenu with game data and username
        Gson gson = new Gson();
        PlayerGameState gameState = gson.fromJson(data.getAsJsonObject().get("playerGameState"), PlayerGameState.class);
        String username = data.getAsJsonObject().get("username").getAsString();
        BaseMenu userMenu = new UserMenu();
        userMenu.setGameData(PlayerGameStateFormatter.format(gameState));
        userMenu.showGameData();
        userMenu.setUsername(username);
        userMenu.setLastMessage(msg);
        userMenu.setCurrAction(null);

        // Send UDP poke and start keepalive so server can register the client's address
        try {
            ClientMain.sendLoginPoke(username);
            // start periodic keepalive to maintain NAT mapping
            ClientMain.startNotificationKeepalive(username);
        } catch (Exception ignored) {
        }

        return userMenu;
    }
}
