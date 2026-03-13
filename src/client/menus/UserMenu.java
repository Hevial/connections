package client.menus;

import java.util.Scanner;
import java.util.function.Supplier;
import models.Request;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserMenu extends BaseMenu {
    private final Map<Integer, Supplier<Request>> requestBuilders;

    public UserMenu(Scanner scanner) {
        super(scanner);
        this.requestBuilders = Map.of(
                1, requestBuilder::buildMakeProposalRequest,
                2, requestBuilder::buildGameStatusRequest,
                3, requestBuilder::buildGameStatsRequest,
                4, requestBuilder::buildPersonalStatsRequest,
                5, requestBuilder::buildLeaderboardRequest,
                6, requestBuilder::buildUpdateCredentialsRequest,
                7, requestBuilder::buildLogoutRequest,
                0, () -> {
                    clearScreen();
                    System.exit(0);
                    return null;
                } // Exit action, will terminate the program
        );
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
        options.put(2, "Stato Partita");
        options.put(3, "Statistiche Partita");
        options.put(4, "Statistiche Personali");
        options.put(5, "Classifica");
        options.put(6, "Aggiorna Credenziali");
        options.put(7, "Logout");
        options.put(0, "Esci");
        return options;
    }

}