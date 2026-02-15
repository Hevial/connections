package client.menus;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Supplier;

import com.google.gson.JsonElement;

import models.Action;
import models.Request;
import models.User;

public class MainMenu extends BaseMenu {

    private final Map<Integer, Supplier<Request>> requestBuilders;

    public MainMenu(Scanner scanner) {
        super(scanner);
        this.requestBuilders = new HashMap<>();
        this.requestBuilders.put(1, this::buildLoginRequest);
        this.requestBuilders.put(2, this::buildRegisterRequest);
        this.requestBuilders.put(3, this::buildUpdateCredentialsRequest);
        this.requestBuilders.put(0, () -> {
            clearScreen();
            System.exit(0);
            return null;
        }); // Exit action, will terminate the program
    }

    @Override
    protected Map<Integer, Supplier<Request>> getRequestBuilders() {
        return requestBuilders;
    }

    @Override
    protected String getMenuTitle() {
        return "MENU PRINCIPALE";
    }

    @Override
    protected Map<Integer, String> getMenuOptions() {
        Map<Integer, String> options = new LinkedHashMap<>();
        options.put(1, "Login");
        options.put(2, "Registrazione");
        options.put(3, "Aggiorna Credenziali");
        options.put(0, "Esci");
        return options;
    }

    private Request buildRegisterRequest() {
        setCurrAction("REGISTRAZIONE");
        User user = requestCredentials("Username: ", "Password: ");
        JsonElement data = gson.toJsonTree(user, User.class);
        return new Request(Action.REGISTER, data);
    }

    private Request buildLoginRequest() {
        setCurrAction("LOGIN");
        User user = requestCredentials("Username: ", "Password: ");
        JsonElement data = gson.toJsonTree(user, User.class);
        return new Request(Action.LOGIN, data);
    }

}