package client.menus;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import client.RequestBuilder;
import client.ClientMain;
import client.InputReader;
import models.AuthRequest;
import models.LeaderboardReq;
import models.Request;

/**
 * Abstract base class for console menus used by the client application.
 *
 * <p>
 * This class provides common UI scaffolding (header, footer, dynamic
 * information), input handling utilities, and a simple request-building
 * mechanism. Concrete menus extend this class and supply the menu title,
 * available options and factories for creating {@link models.Request}
 * instances via {@link #getRequestBuilders()}.
 * </p>
 *
 * <p>
 * Implementations may modify the provided menu state (for example
 * username or lastMessage) and are expected to be used from a single UI
 * thread.
 * </p>
 */
public abstract class BaseMenu {

    protected final RequestBuilder requestBuilder;

    // Session and display state
    protected String lastMessage;
    protected String username;
    protected String currAction;
    protected String gameData;
    protected String generalData;
    protected boolean shouldShowGameData;
    protected boolean shouldShowGeneralData;

    /**
     * Create a new BaseMenu instance.
     *
     * Implementations that extend this class should call the super
     * constructor to initialize the {@link RequestBuilder} helper.
     */
    protected BaseMenu() {
        this.requestBuilder = new RequestBuilder(this);
    }

    /* Getters and Setters */

    /**
     * Set the last informational message to display on the menu.
     *
     * @param message the message to show; may be {@code null} to clear it
     */
    public void setLastMessage(String message) {
        this.lastMessage = message;
    }

    /**
     * Set the username associated with the current session/menu.
     *
     * @param username the username to set; may be {@code null}
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the username associated with the current session/menu.
     *
     * @return the username, or {@code null} if not set
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the current action description displayed on the menu.
     *
     * @param action a short description of the current action; may be {@code null}
     */
    public void setCurrAction(String action) {
        this.currAction = action;
    }

    /**
     * Set the textual game data to be displayed by the menu.
     *
     * @param gameData textual representation of game-related data; may be
     *                 {@code null}
     */
    public void setGameData(String gameData) {
        this.gameData = gameData;
    }

    /**
     * Set general (non-game) data to be displayed by the menu.
     *
     * @param generalData textual data to display; may be {@code null}
     */
    public void setGeneralData(String generalData) {
        this.generalData = generalData;
    }

    /**
     * Mark stored game data to be shown on the next render.
     */
    public void showGameData() {
        this.shouldShowGameData = true;
    }

    /**
     * Hide stored game data from the next render.
     */
    public void hideGameData() {
        this.shouldShowGameData = false;
    }

    /**
     * Mark stored general data to be shown on the next render.
     */
    public void showGeneralData() {
        this.shouldShowGeneralData = true;
    }

    /**
     * Hide stored general data from the next render.
     */
    public void hideGeneralData() {
        this.shouldShowGeneralData = false;
    }

    /* Menu Interface Implementation */

    /**
     * Render the menu to the console.
     *
     * <p>
     * This method performs a full redraw by clearing the screen and
     * printing the header, title, custom content, options, footer and dynamic
     * information in that order.
     * </p>
     */
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

    /**
     * Build and return a {@link models.Request} corresponding to the numeric
     * menu choice.
     *
     * @param choice the numeric option selected by the user
     * @return a {@link models.Request} instance, or {@code null} if the choice
     *         has no associated request builder
     */
    public Request handleChoice(int choice) {
        Supplier<Request> builder = getRequestBuilders().get(choice);

        if (builder == null) {
            resetScreen();
            System.err.println("╠ No request builder for choice: " + choice);
            return null;
        }

        return builder.get();
    }

    /**
     * Prompt the user to select an option from the menu and return the chosen
     * integer value.
     *
     * <p>
     * Returns {@code -1} when input was interrupted by a notification and the
     * caller should retry the menu loop.
     * </p>
     *
     * @return the chosen option index, or {@code -1} if input was interrupted
     */
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

    /**
     * Request a line of input from the user with an optional screen reset
     * after receiving input.
     *
     * <p>
     * If a notification arrives while waiting, the method returns {@code null}
     * and sets the notification as general data to be shown.
     * </p>
     *
     * @param prompt          the prompt text to display
     * @param resetAfterInput if {@code true}, the screen is reset after input
     * @return the trimmed input line, or {@code null} if input was interrupted
     */
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
     * prompts, reads the input from the console, and returns an
     * {@link models.AuthRequest} with the entered values.
     *
     * @param usernamePrompt the prompt message for the username input
     * @param passwordPrompt the prompt message for the password input
     * @return an {@link models.AuthRequest} containing the entered username and
     *         password,
     *         or {@code null} if input was interrupted
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
     * Collect proposal words from the user.
     *
     * <p>
     * Menus that support proposal creation should override this method to
     * provide the required input flow. The default implementation throws
     * {@link UnsupportedOperationException}.
     * </p>
     *
     * @return a list of proposal words
     * @throws UnsupportedOperationException when the menu does not support
     *                                       proposals
     */
    public List<String> getWordsForProposal() {
        throw new UnsupportedOperationException("Proposal input is not supported in this menu");
    }

    /**
     * Obtain a game id from the user.
     *
     * @return the selected game id
     * @throws UnsupportedOperationException when the menu does not support game id
     *                                       input
     */
    public int getGameId() {
        throw new UnsupportedOperationException("Game ID input is not supported in this menu");
    }

    /**
     * Build a {@link models.LeaderboardReq} from user input.
     *
     * @return a leaderboard request
     * @throws UnsupportedOperationException when the menu does not support
     *                                       leaderboard requests
     */
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

    /**
     * Return the human-readable menu title shown in the title area.
     *
     * @return the menu title string
     */
    protected abstract String getMenuTitle();

    /**
     * Return a map of menu option indices to option labels.
     *
     * @return a map where keys are option numbers and values are option labels
     */
    protected abstract Map<Integer, String> getMenuOptions();

    /**
     * Return a map of menu option indices to request builder suppliers.
     *
     * The returned suppliers are invoked by {@link #handleChoice(int)} to
     * construct the {@link models.Request} instance associated with a choice.
     *
     * @return a map of option index to {@link Supplier} of {@link Request}
     */
    protected abstract Map<Integer, Supplier<Request>> getRequestBuilders();

    /**
     * Hook for subclasses to render additional content between the title and
     * the options list. The default implementation does nothing.
     */
    protected void printCustomContent() {
        // optional
    }

    /* Utility Methods */

    /**
     * Clear the console screen in a platform-specific manner.
     *
     * <p>
     * Attempts to run the Windows "cls" command on Windows platforms and
     * emits ANSI clear codes on Unix-like systems. If both approaches fail, a
     * fallback of printing empty lines is used.
     * </p>
     */
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

    /**
     * Reset transient display state and re-render the menu.
     *
     * This clears the last message, clears the screen and calls {@link #show()}.
     */
    protected void resetScreen() {
        lastMessage = null;
        clearScreen();
        show();
    }

}
