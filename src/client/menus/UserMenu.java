package client.menus;

import java.util.function.Supplier;

import models.LeaderboardReq;
import models.Request;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Menu presented to authenticated users.
 *
 * <p>
 * This menu exposes gameplay and account-related actions available to a
 * logged-in user, including making proposals, viewing game status, requesting
 * statistics, viewing the leaderboard, updating credentials and logging out.
 * Concrete request builders are provided via {@link #getRequestBuilders()}.
 * </p>
 *
 * @see BaseMenu
 */
public class UserMenu extends BaseMenu {
    /**
     * Map of menu option index to {@link Supplier} that produces the
     * corresponding {@link models.Request} for that option.
     */
    private final Map<Integer, Supplier<Request>> requestBuilders;

    /**
     * Construct the user menu and register request builders for each option.
     *
     * <p>
     * Option {@code 0} performs a program exit.
     * </p>
     */
    public UserMenu() {
        super();
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

    /**
     * Return the mapping of menu option indices to request builders.
     *
     * @return a map where keys are option numbers and values are suppliers
     *         producing the corresponding {@link models.Request}
     */
    @Override
    protected Map<Integer, Supplier<Request>> getRequestBuilders() {
        return requestBuilders;
    }

    /**
     * Return the menu title displayed in the header area.
     *
     * @return the menu title string
     */
    @Override
    protected String getMenuTitle() {
        return "MENU UTENTE";
    }

    /**
     * Return a map of menu options where the key is the option number and the
     * value is the label shown to the user.
     *
     * @return ordered map of option index to label
     */
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

    /**
     * Collect proposal words from the user.
     *
     * <p>
     * Prompts the user to enter exactly four comma-separated words, normalizes
     * them to upper case and validates that none are empty and all are unique.
     * If input is interrupted by a notification or cancelled, this method
     * returns {@code null}.
     * </p>
     *
     * @return a list of four normalized words, or {@code null} if input was
     *         interrupted
     */
    @Override
    public List<String> getWordsForProposal() {
        resetScreen();
        while (true) {
            showGameData();
            String wordsString = requestInput(
                    "Inserisci le parole per la proposta (esattamente 4 parole separate da virgola): ", true);
            if (wordsString == null) {
                // either notification arrived or user cancelled
                return null;
            }
            String[] rawWords = wordsString.split(",");

            if (rawWords.length != 4) {
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
                System.out.println("╠ Input non valido: le parole non possono essere vuote.");
                continue;
            }

            if (new HashSet<>(words).size() != 4) {
                System.out.println("╠ Input non valido: le 4 parole devono essere diverse.");
                continue;
            }

            return words;
        }
    }

    /**
     * Prompt the user to enter a game id.
     *
     * <p>
     * Accepts {@code -1} to refer to the current game. If input is
     * interrupted by a notification this method returns {@link Integer#MIN_VALUE}
     * to signal the caller that the operation should be retried.
     * </p>
     *
     * @return the selected game id, {@code -1} for current game, or
     *         {@code Integer.MIN_VALUE} if input was interrupted
     */
    @Override
    public int getGameId() {
        int gameId = -1;
        String input = requestInput("Inserisci l'ID della partita da visualizzare (-1 partita attuale): ", true);
        if (input == null) {
            // interrupted by notification
            return Integer.MIN_VALUE;
        }
        input = input.trim();
        while (true) {
            try {
                gameId = Integer.parseInt(input);
                if (gameId >= -1) {
                    break; // valid id entered
                } else {
                    // negative id; prompt again
                    input = requestInput("ID non valido. Inserisci un id numerico positivo (o -1 partita attuale): ",
                            true);
                    if (input == null)
                        return Integer.MIN_VALUE;
                }
            } catch (NumberFormatException e) {
                // non-numeric input; prompt again
                input = requestInput("Input non valido. Inserisci un id numerico positivo (o -1 partita attuale): ",
                        true);
                if (input == null)
                    return Integer.MIN_VALUE;
            }
        }
        return gameId;
    }

    /**
     * Interactively build and return a {@link models.LeaderboardReq}.
     *
     * <p>
     * The method offers three flows: general leaderboard, top-K listing,
     * and player position lookup. It validates user input and returns
     * {@code null} if the input was interrupted by a notification.
     * </p>
     *
     * @return a {@link models.LeaderboardReq} or {@code null} if input was
     *         interrupted
     */
    @Override
    public LeaderboardReq getLeaderboardRequest() {
        resetScreen();
        String choises = """
                ╠ Scegli la classifica da visualizzare:
                ║ 1. Classifica generale
                ║ 2. Top K in classifica
                ║ 3. Posizione giocatore in classifica
                ║
                """;

        System.out.print(choises);
        String input = requestInput("Inserisci la tua scelta: ", true);
        if (input == null)
            return null;

        while (!input.matches("[1-3]")) {
            System.out.print(choises);
            input = requestInput("Scelta non valida. Inserisci 1, 2 o 3: ", true);
            if (input == null)
                return null;
        }

        if (input.equals("1")) {
            return new LeaderboardReq();
        } else if (input.equals("2")) {
            int k = -1;
            while (k <= 0) {
                String kInput = requestInput("Inserisci il numero di top giocatori da visualizzare (K): ", true);
                if (kInput == null)
                    return null;

                if (kInput.trim().isEmpty()) {
                    continue;
                }
                try {
                    k = Integer.parseInt(kInput.trim());
                    if (k <= 0) {
                        System.out.println("║ Input non valido. K deve essere un numero positivo.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("║ Input non valido. Inserisci un numero positivo per K.");
                }
            }
            return new LeaderboardReq(k);
        } else {
            String playerName = requestInput("Inserisci il nome del giocatore: ", true);
            while (playerName == null || playerName.isEmpty()) {
                playerName = requestInput("Input non valido. Inserisci un nome di giocatore valido: ", true);
            }

            return new LeaderboardReq(playerName.trim());
        }
    }
}