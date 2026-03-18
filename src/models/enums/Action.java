package models.enums;

/**
 * Enumeration of actions that can be requested by a client.
 *
 * <p>Each constant identifies an operation handled by the server-side request
 * handlers. The associated request payload and response format depend on the
 * specific action.
 */
public enum Action {
    REGISTER,
    LOGIN,
    LOGOUT,
    UPDATE_CREDENTIALS,
    SUBMIT_PROPOSAL,
    GAME_STATUS,
    GAME_STATS,
    LEADERBOARD,
    PERSONAL_STATS
}
