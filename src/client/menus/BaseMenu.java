package client.menus;

import java.util.List;
import java.util.Map;
/* Scanner removed: InputReader handles System.in */
import java.util.function.Supplier;

import client.RequestBuilder;
import client.ClientMain;
import client.InputReader;
import models.AuthRequest;
import models.LeaderboardReq;
import models.Request;
import models.User;

/**
 * BaseMenu is an abstract class that provides a foundation for implementing
 * menu-driven user interfaces in a client application. It manages user input,
 * screen clearing, and request building logic, delegating specific request
 * creation to subclasses via the {@link #getRequestBuilders()} method.
 * 
 * <p>
 * Key responsibilities:
 * <ul>
 * <li>Handles user input and menu selection.</li>
 * <li>Manages screen clearing and message display.</li>
 * <li>Provides utility methods for credential input and request handling.</li>
 * <li>Requires subclasses to define available request builders.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Fields include references to the input scanner, user session data, and
 * the last message displayed. This class is intended to be extended by
 * concrete menu implementations.
 * </p>
 * 
 */
public abstract class BaseMenu {

    // Input is handled centrally by InputReader; no Scanner stored here.
    protected final RequestBuilder requestBuilder;

    // Session and display state
    protected String lastMessage;
    protected String username;
    protected String currAction;
    protected String gameData;
    protected String generalData;
    protected boolean shouldShowGameData;
    protected boolean shouldShowGeneralData;

    protected BaseMenu() {
        this.requestBuilder = new RequestBuilder(this);
    }

    /* Getters and Setters */

    public void setLastMessage(String message) {
        this.lastMessage = message;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setCurrAction(String action) {
        this.currAction = action;
    }

    public void setGameData(String gameData) {
        this.gameData = gameData;
    }

    public void setGeneralData(String generalData) {
        this.generalData = generalData;
    }

    public void showGameData() {
        this.shouldShowGameData = true;
    }

    public void hideGameData() {
        this.shouldShowGameData = false;
    }

    public void showGeneralData() {
        this.shouldShowGeneralData = true;
    }

    public void hideGeneralData() {
        this.shouldShowGeneralData = false;
    }

    /* Menu Interface Implementation */

    public final void show() {
        clearScreen();
        System.out.println();

        printHeader();
        printTitle();
        printCustomContent();
        printOptions();
        printFooter();
        printDynamicInfo();
    }

    public Request handleChoice(int choice) {
        Supplier<Request> builder = getRequestBuilders().get(choice);

        if (builder == null) {
            resetScreen();
            System.err.println("╠ No request builder for choice: " + choice);
            return null;
        }

        return builder.get();
    }

    public int getChoice() {
        setCurrAction(null);
        String prompt = "Seleziona un'opzione: ";
        while (true) {
            String input = requestInput(prompt, true);
            if (input == null) {
                // notification interrupted input -> signal caller to retry menu loop
                return -1;
            }
            if (input.isBlank()) {
                resetScreen();
                continue;
            }
            try {
                int choice = Integer.parseInt(input.trim());
                if (getRequestBuilders().containsKey(choice)) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("╠ Scelta non valida. Riprova.");
        }
    }

    public String requestInput(String prompt, boolean resetAfterInput) {
        System.out.print("╠ " + prompt);
        while (true) {
            String line = InputReader.pollLine(200);
            if (line == null) {
                String pending = ClientMain.consumeNotification();
                if (pending != null) {
                    resetScreen();
                    setGeneralData(pending);
                    showGeneralData();
                    return null;
                }
                continue;
            }

            if (resetAfterInput) {
                resetScreen();
            }

            return line.trim();
        }
    }

    /**
     * Prompts the user for credentials using the provided username and password
     * prompts,
     * reads the input from the console, and returns a {@link User} object with the
     * entered values.
     *
     * @param usernamePrompt the prompt message for the username input
     * @param passwordPrompt the prompt message for the password input
     * @return a {@link User} object containing the entered username and password
     */
    public AuthRequest requestCredentials(String usernamePrompt, String passwordPrompt) {
        resetScreen();
        String username = requestInput(usernamePrompt, false);
        if (username == null)
            return null;
        String password = requestInput(passwordPrompt, true);
        if (password == null)
            return null;
        return new AuthRequest(username, password);
    }

    /**
     * Returns proposal words collected from UI.
     *
     * <p>
     * Only menus that support proposal creation should override this method.
     * </p>
     *
     * @return list of words for the proposal
     */
    public List<String> getWordsForProposal() {
        throw new UnsupportedOperationException("Proposal input is not supported in this menu");
    }

    public int getGameId() {
        throw new UnsupportedOperationException("Game ID input is not supported in this menu");
    }

    public LeaderboardReq getLeaderboardRequest() {
        throw new UnsupportedOperationException("Leaderboard request is not supported in this menu");
    }

    /* Template Methods for Menu Display */

    protected void printHeader() {
        System.out.println("╔═════════════════════════════════╗");
        System.out.println("║                                 ║");
        System.out.println("║           CONNECTIONS           ║");
        System.out.println("║                                 ║");
        System.out.println("╠═════════════════════════════════╣");
    }

    protected void printTitle() {
        System.out.printf("║ %-31s ║\n", getMenuTitle());
        System.out.println("╠═════════════════════════════════╣");
    }

    protected void printOptions() {
        for (Map.Entry<Integer, String> entry : getMenuOptions().entrySet()) {
            System.out.printf("║ %-2s %-28s ║\n",
                    entry.getKey() + ".",
                    entry.getValue());
        }
    }

    protected void printFooter() {
        System.out.println("╠═════════════════════════════════╝");
    }

    /**
     * Prints dynamic information to the console based on the current state.
     * 
     * This method displays optional information including the last message,
     * current action, username, and game data. Each piece of information is
     * only printed if it exists (is not null or, for messages, not blank).
     * 
     * The output is formatted with box-drawing characters (╠ and ║) for
     * visual organization in the console.
     */
    protected void printDynamicInfo() {
        if (lastMessage != null && !lastMessage.isBlank())
            System.out.println("╠ Messaggio: " + lastMessage + "\n║");

        if (currAction != null)
            System.out.println("╠ Azione: " + currAction + "\n║");

        if (username != null)
            System.out.println("╠ Utente: " + username + "\n║");

        if (gameData != null && shouldShowGameData) {
            if (shouldShowGeneralData) {
                shouldShowGeneralData = false;
            }

            System.out.print(gameData);
            shouldShowGameData = false;
        }

        if (generalData != null && shouldShowGeneralData) {
            System.out.print(generalData);
            shouldShowGeneralData = false;
        }

        System.out.println("║");
    }

    /* Template Hooks */

    protected abstract String getMenuTitle();

    protected abstract Map<Integer, String> getMenuOptions();

    protected abstract Map<Integer, Supplier<Request>> getRequestBuilders();

    protected void printCustomContent() {
        // opzionale
    }

    /* Utility Methods */

    protected void clearScreen() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                // Windows: usa il comando cls
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Unix/Linux/macOS: sequenza ANSI
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback: stampa righe vuote
            for (int i = 0; i < 50; i++)
                System.out.println();
        }
    }

    protected void resetScreen() {
        lastMessage = null;
        clearScreen();
        show();
    }

}
