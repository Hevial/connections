package models.enums;

/**
 * Enumeration of actions that can be presented in the client's menus.
 *
 * <p>Each enum constant carries a human-readable display name intended for
 * presentation in the user interface. Display names in this repository are
 * currently written in Italian; callers that render UI elements may translate
 * these strings for other locales if required.</p>
 *
 * @see #getDisplayName()
 */
public enum MenuAction {
    REGISTER("REGISTRAZIONE"),
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    UPDATE_CREDENTIALS("AGGIORNA CREDENZIALI"),
    MAKE_PROPOSAL("FAI UNA PROPOSTA"),
    REQUEST_GAME_STATUS("RICHIEDI STATO PARTITA"),
    REQUEST_GAME_STATS("RICHIEDI STATISTICHE PARTITA"),
    REQUEST_LEADERBOARD("RICHIEDI CLASSIFICA"),
    REQUEST_PERSONAL_STATS("RICHIEDI STATISTICHE PERSONALI");

    /** Human-readable label suitable for displaying in menus. */
    private final String displayName;

    /**
     * Create a menu action with the given display name.
     *
     * @param displayName a short label used when rendering this action in the UI
     */
    MenuAction(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the human-readable label for this action.
     *
     * @return the display name associated with this action (never {@code null})
     */
    public String getDisplayName() {
        return displayName;
    }
}