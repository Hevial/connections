package server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import models.Game;
import models.GameState;
import models.PlayerGameState;
import server.db.DBManager;

/**
 * Coordinates the active game round lifecycle on the server.
 * <p>
 * This component is scheduled periodically and is responsible for loading the
 * next game, publishing the current {@link GameState}, and resetting live
 * per-player states for the new round.
 * </p>
 */
public class GameManager implements Runnable {

    private final int gameDuration;
    /**
     * Shared snapshot of the currently active game round.
     * <p>
     * Marked as {@code volatile} because it is written by the scheduler thread
     * and read by request-handling threads.
     * </p>
     */
    private volatile GameState currentGameState;
    /**
     * Live per-player state for the current round keyed by userId.
     */
    private final Map<String, PlayerGameState> playerStatesByUserId;

    public GameManager(int gameDuration) {
        this.gameDuration = gameDuration;
        this.playerStatesByUserId = new ConcurrentHashMap<>();
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    /**
     * Returns the live player state for the given user, creating it if missing.
     * <p>
     * Player state is initialized lazily from the current game snapshot. If the
     * user has a stale state (old gameId), it is regenerated for the active round.
     * The remaining time is refreshed from the latest round snapshot on each call.
     * </p>
     *
     * @param userId user identifier
     * @return live player state for the active round, or {@code null} when userId
     *         is invalid or no round is currently active
     */
    public PlayerGameState getOrCreatePlayerState(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }

        GameState snapshot = currentGameState;
        if (snapshot == null || snapshot.getGame() == null) {
            return null;
        }

        // Atomic per-key update: avoids race conditions between read/check/write
        // when multiple request threads access the same userId concurrently.
        return playerStatesByUserId.compute(userId, (id, existingState) -> {
            // Create state lazily (first access) or regenerate it if this state
            // belongs to a previous round (different gameId).
            if (existingState == null || existingState.getGameId() != snapshot.getGameId()) {
                return new PlayerGameState(
                        snapshot.getGameId(),
                        snapshot.getAllWordsShuffled(),
                        snapshot.getRemainingTime(),
                        snapshot.getGame().getGroups());
            }

            // Same round: keep progress but refresh remaining time from current snapshot.
            existingState.setTimeLeft(snapshot.getRemainingTime());
            return existingState;
        });
    }

    @Override
    public void run() {

        DBManager dbManager = DBManager.getInstance();

        try {
            Game currentGame = dbManager.loadNextGame();
            currentGameState = new GameState(currentGame, System.currentTimeMillis(), gameDuration);

            // New round starts here: drop old in-memory player states.
            playerStatesByUserId.clear();
            System.out.println("\nNew game started: ");
            currentGameState.printGameState();
        } catch (Exception e) {
            System.out.println("GameManager interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

}
