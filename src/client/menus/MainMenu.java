package client.menus;

import java.util.LinkedHashMap;
import java.util.Map;
// Scanner no longer required; InputReader handles input
import java.util.function.Supplier;

import models.Request;

/**
 * Main menu shown to unauthenticated users.
 *
 * <p>This menu exposes the primary actions available before authentication:
 * login, registration, credential update, and exit. It supplies concrete
 * {@link models.Request} factories via {@link #getRequestBuilders()} which are
 * invoked by the base menu framework when the user selects an option.</p>
 *
 * @see BaseMenu
 */
public class MainMenu extends BaseMenu {

    /**
     * Map of menu option index to {@link Supplier} that builds the
     * corresponding {@link models.Request} for that option.
     */
    private final Map<Integer, Supplier<Request>> requestBuilders;

    /**
     * Construct the main menu and register request builders for each option.
     *
     * <p>Option {@code 0} performs a program exit.</p>
     */
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
        return "MENU PRINCIPALE";
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
        options.put(1, "Login");
        options.put(2, "Registrazione");
        options.put(3, "Aggiorna Credenziali");
        options.put(0, "Esci");
        return options;
    }

}