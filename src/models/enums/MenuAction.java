package models.enums;

// TODO: Consider removing unused actions or adding new ones as needed
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

    private final String displayName;

    MenuAction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}