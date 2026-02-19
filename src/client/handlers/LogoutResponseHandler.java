package client.handlers;

import java.util.Scanner;

import com.google.gson.JsonElement;

import client.menus.BaseMenu;
import client.menus.MainMenu;
import models.enums.StatusCodes;

public class LogoutResponseHandler implements ResponseActionHandler {

    private final Scanner scanner;

    public LogoutResponseHandler(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu) {
        String msg = statusCode.toString() + " (" + statusCode.getCode() + "), " + message;

        if (statusCode == StatusCodes.SUCCESS) {
            BaseMenu newMenu = new MainMenu(scanner);
            newMenu.setLastMessage(msg);
            newMenu.setCurrAction(null);
            return newMenu;
        }

        currentMenu.setLastMessage(msg);
        currentMenu.setCurrAction(null);
        return currentMenu;
    }

}
