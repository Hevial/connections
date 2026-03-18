package server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import models.CompletedGame;
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
     * Counts how many players are currently in progress in the live per-user
     * states.
     *
     * Iterates over {@code playerStatesByUserId} and increments the counter for
     * each {@link PlayerGameState} that is not completed (i.e.
     * {@code !state.isCompleted()}).
     * The result is a best-effort snapshot: the underlying
     * {@code ConcurrentHashMap}
     * may be concurrently modified by other threads.
     *
     * @return the number of players still playing in the active round
     */
    public int getPlayersInProgress() {
        int count = 0;
        for (PlayerGameState state : playerStatesByUserId.values()) {
            if (!state.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts how many players have completed the current round.
     *
     * Iterates over {@code playerStatesByUserId} and counts entries where
     * {@link PlayerGameState#isCompleted()} returns {@code true}.
     * The returned value is a snapshot and may change if states are updated
     * concurrently by other threads.
     *
     * @return the number of players who have completed the active round
     */
    public int getPlayersCompleted() {
        int count = 0;
        for (PlayerGameState state : playerStatesByUserId.values()) {
            if (state.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts how many players are marked as winners in the current round.
     *
     * Iterates over {@code playerStatesByUserId} and increments the counter for
     * each {@link PlayerGameState} where {@code state.isWinner()} is {@code true}.
     * As with other counters, the result is a concurrent snapshot and may vary
     * if player states are modified concurrently.
     *
     * @return the number of players who won the active round
     */
    public int getPlayersWon() {
        int count = 0;
        for (PlayerGameState state : playerStatesByUserId.values()) {
            if (state.isWinner()) {
                count++;
            }
        }
        return count;
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

    // private void saveInGameHistory() {

    // DBManager dbManager = DBManager.getInstance();
    // CompletedGame completedGame = new CompletedGame(currentGameState.getGameId(),
    // playerStatesByUserId,
    // currentGameState.getGame().getGroups());
    // try {
    // dbManager.saveGameHistory(completedGame);
    // } catch (Exception e) {
    // System.out.println("Failed to save game history: " + e.getMessage());
    // }

    // }

    // private void updatePlayersStats() {
    // DBManager dbManager = DBManager.getInstance();
    // try {
    // dbManager.updateUsersStats(playerStatesByUserId);
    // } catch (Exception e) {
    // System.out.println("Failed to update users stats: " + e.getMessage());
    // }
    // }

    @Override
    public void run() {

        DBManager dbManager = DBManager.getInstance();

        try {

            // Before starting a new round, save the completed game to history and update
            // player stats in the database. This ensures that we capture the final state of
            // the game and player performance before resetting for the next round.
            CompletedGame completedGame = null;
            if (currentGameState != null) {
                completedGame = new CompletedGame(currentGameState.getGameId(), playerStatesByUserId,
                        currentGameState.getGame().getGroups());

                try {
                    dbManager.saveGameHistory(completedGame);
                    dbManager.updateUsersStats(playerStatesByUserId);
                } catch (Exception e) {
                    System.out.println("Failed to save game history or update stats: " + e.getMessage());
                }

            }

            playerStatesByUserId.clear(); // Clear live player states for the new round

            Game currentGame = dbManager.loadNextGame();
            currentGameState = new GameState(currentGame, System.currentTimeMillis(), gameDuration);

            // Notify clients about the completed game and provide the new game snapshot.
            if (completedGame != null) {
                try {
                    NotificationSender.notifyGameEnd(completedGame, currentGameState);
                } catch (Exception e) {
                    System.out.println("Failed to send game-end notifications: " + e.getMessage());
                }
            }

            // TODO CHECK IF CLEARING PLAYER STATES BEFORE OR AFTER LOADING THE NEW GAME IS
            // BETTER
            System.out.println("\nNew game started: ");
            currentGameState.printGameState();
        } catch (Exception e) {
            System.out.println("GameManager interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

}
