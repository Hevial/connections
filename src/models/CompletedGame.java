package models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a completed game, encapsulating the final state of the game and
 * the individual states of each player.
 * <p>
 * This class provides access to the final game state and allows querying player
 * participation.
 * </p>
 * 
 */
/**
 * Represents the final state of a completed game, including statistics and
 * player results.
 * <p>
 * This class encapsulates information about the game such as the number of
 * players,
 * winners, completers, average score, and the groups involved. It also
 * maintains
 * per-player statistics mapped by user ID.
 * </p>
 *
 * <ul>
 * <li>{@code gameId}: Unique identifier for the completed game.</li>
 * <li>{@code numberOfPlayers}: Total number of players who participated.</li>
 * <li>{@code numberOfWinners}: Number of players who won the game.</li>
 * <li>{@code numberOfCompleters}: Number of players who completed the
 * game.</li>
 * <li>{@code averageScore}: Average score achieved by all players.</li>
 * <li>{@code groups}: List of groups relevant to the game.</li>
 * <li>{@code playerStatsByUserId}: Mapping of user IDs to their game
 * statistics.</li>
 * </ul>
 *
 * <p>
 * The constructor processes player states and computes aggregate statistics.
 * </p>
 *
 */
public class CompletedGame {

    private final int gameId;
    private final int numberOfPlayers;
    private final int numberOfWinners;
    private final int numberOfCompleters;
    private final double averageScore;
    private final List<Group> groups;

    // userId -> PlayerGameStats
    private final Map<String, PlayerGameStats> playerStatsByUserId;

    public CompletedGame(int gameId, Map<String, PlayerGameState> playerStatesByUserId, List<Group> groups) {
        this.groups = groups;
        this.gameId = gameId;

        this.numberOfPlayers = playerStatesByUserId.size();
        this.playerStatsByUserId = new HashMap<>(numberOfPlayers);

        int winners = 0;
        int completers = 0;
        int totalScore = 0;

        for (Map.Entry<String, PlayerGameState> entry : playerStatesByUserId.entrySet()) {
            String userId = entry.getKey();
            PlayerGameState state = entry.getValue();

            if (state.isWinner()) {
                winners++;
            }

            if (state.isComplete()) {
                completers++;
            }

            totalScore += state.getScore();

            PlayerGameStats stats = new PlayerGameStats(
                    state.getGroupsFound().size(),
                    state.getMistakes(),
                    state.getScore());

            playerStatsByUserId.put(userId, stats);
        }

        this.numberOfWinners = winners;
        this.numberOfCompleters = completers;
        this.averageScore = numberOfPlayers > 0 ? (double) totalScore / numberOfPlayers : 0.0;
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
        return playerStatsByUserId.containsKey(userId);
    }

    /**
     * Returns the total number of players who participated in the completed game.
     *
     * @return the number of players
     */
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    /**
     * Returns the number of players who won the completed game.
     *
     * @return the number of winners
     */
    public int getNumberOfWinners() {
        return numberOfWinners;
    }

    /**
     * Returns the number of players who completed the game (regardless of
     * win/loss).
     *
     * @return the number of completers
     */
    public int getNumberOfCompleters() {
        return numberOfCompleters;
    }

    /**
     * Returns the average score achieved by all players in the completed game.
     *
     * @return the average score
     */
    public double getAverageScore() {
        return averageScore;
    }

    /**
     * Returns the list of groups associated with the completed game.
     * <p>
     * Each group contains the correct assignment of words for the game solution.
     * The returned list is immutable.
     * </p>
     *
     * @return an immutable list of groups
     */
    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    /**
     * Returns the {@link models.PlayerGameStats} for the specified user.
     *
     * @param userId the user id to look up
     * @return the PlayerGameStats for the user, or {@code null} if the user is not
     *         present
     */
    public PlayerGameStats getPlayerStats(String userId) {
        return playerStatsByUserId.get(userId);
    }
}
