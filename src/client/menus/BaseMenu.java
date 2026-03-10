package client.menus;

import java.util.Map;
import java.util.Scanner;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import models.AuthRequest;
import models.Request;
import models.User;
import models.enums.Action;

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

    protected final Scanner scanner;

    // Session and display state
    protected String lastMessage;
    protected String username;
    protected String currAction;
    protected String data;

    protected Gson gson = new Gson();

    protected BaseMenu(Scanner scanner) {
        this.scanner = scanner;
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

    public void setData(String data) {
        this.data = data;
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
        String prompt = "╠ Seleziona un'opzione: ";
        System.out.print(prompt);

        while (true) {
            if (!scanner.hasNextLine()) {
                // TODO EOF: esci o tratta come logout
                return 0;
            }
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (getRequestBuilders().containsKey(choice)) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
            }
            resetScreen();
            System.out.print("╠ Scelta non valida. Riprova: ");
        }
    }

    public Request buildUpdateCredentialsRequest() {
        setCurrAction("AGGIORNA CREDENZIALI");
        AuthRequest oldUser = requestCredentials("Vecchio Username: ", "Vecchia Password: ");
        AuthRequest newUser = requestCredentials("Nuovo Username: ", "Nuova Password: ");

        if (newUser.getUsername().isBlank() && !newUser.getPassword().isBlank())
            newUser.setUsername(oldUser.getUsername());

        if (newUser.getPassword().isBlank() && !newUser.getUsername().isBlank())
            newUser.setPassword(oldUser.getPassword());

        JsonElement data = gson.toJsonTree(Map.of("oldUser", oldUser, "newUser", newUser));
        return new Request(Action.UPDATE_CREDENTIALS, data);
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

    protected void printDynamicInfo() {
        if (lastMessage != null && !lastMessage.isBlank())
            System.out.println("╠ Messaggio: " + lastMessage);

        if (currAction != null)
            System.out.println("╠ Azione: " + currAction);

        if (username != null)
            System.out.println("╠ Utente: " + username);

        if (data != null)
            System.out.println("╠ Dati: " + data);

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
    protected AuthRequest requestCredentials(String usernamePrompt, String passwordPrompt) {
        resetScreen();
        System.out.print("╠ " + usernamePrompt);
        String username = scanner.nextLine();
        System.out.print("╠ " + passwordPrompt);
        String password = scanner.nextLine();
        return new AuthRequest(username, password);
    }

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

    // TODO: Maybe this should also reset username, currAction and data?
    protected void resetScreen() {
        lastMessage = null;
        clearScreen();
        show();
    }

}
