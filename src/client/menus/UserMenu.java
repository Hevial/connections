package client.menus;

import java.util.Scanner;
import java.util.function.Supplier;

import models.Request;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserMenu extends BaseMenu {
    private final Map<Integer, Supplier<Request>> requestBuilders;

    public UserMenu(Scanner scanner) {
        super(scanner);
        this.requestBuilders = new HashMap<>();
        this.requestBuilders.put(5, this::buildUpdateCredentialsRequest);
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
        return "MENU UTENTE";
    }

    @Override
    protected Map<Integer, String> getMenuOptions() {
        Map<Integer, String> options = new LinkedHashMap<>();
        options.put(1, "Fai Proposta");
        options.put(2, "Informazioni Partita");
        options.put(3, "Statistiche Partita");
        options.put(4, "Statistiche Globali");
        options.put(5, "Aggiorna Credenziali");
        options.put(6, "Logout");
        options.put(0, "Esci");
        return options;
    }
}