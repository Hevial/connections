package client.handlers;

import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import client.PlayerGameStateFormatter;
import client.menus.BaseMenu;
import client.menus.UserMenu;
import models.PlayerGameState;
import models.enums.StatusCodes;

public class LoginResponseHandler implements ResponseActionHandler {

    private final Scanner scanner;

    public LoginResponseHandler(Scanner scanner) {
        this.scanner = scanner;
    }

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

        // If login was successful, transition to the user menu
        Gson gson = new Gson();
        PlayerGameState gameState = gson.fromJson(data.getAsJsonObject().get("playerGameState"), PlayerGameState.class);
        String username = data.getAsJsonObject().get("username").getAsString();
        BaseMenu userMenu = new UserMenu(scanner);
        userMenu.setData(PlayerGameStateFormatter.format(gameState));
        userMenu.showGameData();
        userMenu.setUsername(username);
        userMenu.setLastMessage(msg);
        userMenu.setCurrAction(null);

        return userMenu;
    }
}
