package client.menus;

import java.util.LinkedHashMap;
import java.util.Map;
// Scanner no longer required; InputReader handles input
import java.util.function.Supplier;

import models.Request;

/**
 * Main entry menu shown to unauthenticated users.
 *
 * <p>
 * Provides options to login, register, update credentials or exit the
 * application. Subclasses of {@link BaseMenu} implement the concrete
 * request builders used by the menu actions.
 * </p>
 */
public class MainMenu extends BaseMenu {

    private final Map<Integer, Supplier<Request>> requestBuilders;

    public MainMenu() {
        super();
        this.requestBuilders = Map.of(
                1, requestBuilder::buildLoginRequest,
                2, requestBuilder::buildRegisterRequest,
                3, requestBuilder::buildUpdateCredentialsRequest,
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

}