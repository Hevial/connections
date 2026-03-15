package server;

import java.util.Map;
import java.util.Set;

import models.PlayerGameState;

/**
 * Represents a completed game, encapsulating the final state of the game and
 * the individual states of each player.
 * <p>
 * This class provides access to the final game state and allows querying player
 * participation.
 * </p>
 * 
 */
public class CompletedGame {

    private final int gameId;
    private final int numberOfPlayers;
    private final int numberOfWinners;
    private final int numberOfCompleters;
    private final double averageScore;

    private final Set<String> playerIds;

    public CompletedGame(int gameId, Map<String, PlayerGameState> playerStatesByUserId) {
        playerIds = Set.copyOf(playerStatesByUserId.keySet());
        this.gameId = gameId;
        this.numberOfPlayers = playerStatesByUserId.size();
        this.numberOfWinners = (int) playerStatesByUserId.values().stream().filter(PlayerGameState::isWinner).count();
        this.numberOfCompleters = (int) playerStatesByUserId.values().stream().filter(PlayerGameState::isComplete)
                .count();
        this.averageScore = playerStatesByUserId.values().stream().mapToInt(PlayerGameState::getScore).average()
                .orElse(0.0);
    }

    /**
     * Retrieves the unique identifier of the completed game.
     *
     * @return the game ID from the final state
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * Checks if a player with the specified user ID is present in the game.
     *
     * @param userId the unique identifier of the player to check
     * @return {@code true} if the player is present; {@code false} otherwise
     */
    public boolean isPlayerPresent(String userId) {
        return playerIds.contains(userId);
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public int getNumberOfWinners() {
        return numberOfWinners;
    }

    public int getNumberOfCompleters() {
        return numberOfCompleters;
    }

    public double getAverageScore() {
        return averageScore;
    }
}
