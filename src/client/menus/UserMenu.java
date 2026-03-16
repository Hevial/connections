package client.menus;

import java.util.Scanner;
import java.util.function.Supplier;
import models.Request;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserMenu extends BaseMenu {
    private final Map<Integer, Supplier<Request>> requestBuilders;

    public UserMenu(Scanner scanner) {
        super(scanner);
        this.requestBuilders = Map.of(
                1, requestBuilder::buildMakeProposalRequest,
                2, requestBuilder::buildCurrentGameStatusRequest,
                3, requestBuilder::buildGameStatusByIdRequest,
                4, requestBuilder::buildGameStatsRequest,
                5, requestBuilder::buildPersonalStatsRequest,
                6, requestBuilder::buildLeaderboardRequest,
                7, requestBuilder::buildUpdateCredentialsRequest,
                8, requestBuilder::buildLogoutRequest,
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
        options.put(2, "Stato Partita Attuale");
        options.put(3, "Stato Partita per Id");
        options.put(4, "Statistiche Partita");
        options.put(5, "Statistiche Personali");
        options.put(6, "Classifica");
        options.put(7, "Aggiorna Credenziali");
        options.put(8, "Logout");
        options.put(0, "Esci");
        return options;
    }

    @Override
    public List<String> getWordsForProposal() {
        resetScreen();
        while (true) {
            showGameData();
            System.out.print(
                    "╠ Inserisci le parole per la proposta (esattamente 4 parole separate da virgola): ");

            String wordsString = scanner.nextLine().trim();
            String[] rawWords = wordsString.split(",");

            if (rawWords.length != 4) {
                resetScreen();
                System.out.println("╠ Input non valido: devi inserire esattamente 4 parole separate da virgola.");
                continue;
            }

            List<String> words = new ArrayList<>();
            boolean hasEmptyWord = false;

            for (String rawWord : rawWords) {
                String normalizedWord = rawWord.trim().toUpperCase();
                if (normalizedWord.isEmpty()) {
                    hasEmptyWord = true;
                    break;
                }
                words.add(normalizedWord);
            }

            if (hasEmptyWord) {
                resetScreen();
                System.out.println("╠ Input non valido: le parole non possono essere vuote.");
                continue;
            }

            if (new java.util.HashSet<>(words).size() != 4) {
                resetScreen();
                System.out.println("╠ Input non valido: le 4 parole devono essere diverse.");
                continue;
            }

            return words;
        }
    }

    @Override
    public int getGameId() {
        int gameId = -1;
        String input = requestInput("Inserisci l'ID della partita da visualizzare (-1 partita attuale): ");
        input = input.trim();
        while (true) {
            try {
                gameId = Integer.parseInt(input);
                if (gameId >= -1) {
                    break; // valid id entered
                } else {
                    // negative id; prompt again
                    input = requestInput("ID non valido. Inserisci un id numerico positivo (o -1 partita attuale): ");
                }
            } catch (NumberFormatException e) {
                // non-numeric input; prompt again
                input = requestInput("Input non valido. Inserisci un id numerico positivo (o -1 partita attuale): ");
            }
        }
        return gameId;
    }

}