package models;

import java.util.List;

/**
 * View model exposing a completed game's summary from a single player's
 * perspective.
 *
 * <p>
 * This DTO contains global aggregates (number of players, winners,
 * average score) plus the {@link PlayerGameStats} for the requesting player
 * when available.
 * </p>
 */
public class PlayerCompletedGame {

    public final int gameId;
    public final int numberOfPlayers;
    public final int numberOfWinners;
    public final int numberOfCompleters;
    public final double averageScore;
    public final List<Group> groups;
    public final PlayerGameStats playerStats; // null if the player did not participate in the game

    /**
     * Create a player-centric completed game representation.
     *
     * @param gameId             unique game id
     * @param numberOfPlayers    total players in the game
     * @param numberOfWinners    players who won
     * @param numberOfCompleters players who completed
     * @param averageScore       average score across players
     * @param groups             list of groups from the game
     * @param playerStats        per-player stats for the requesting user (may be
     *                           null)
     */
    public PlayerCompletedGame(int gameId, int numberOfPlayers, int numberOfWinners,
            int numberOfCompleters, double averageScore,
            List<Group> groups, PlayerGameStats playerStats) {
        this.gameId = gameId;
        this.numberOfPlayers = numberOfPlayers;
        this.numberOfWinners = numberOfWinners;
        this.numberOfCompleters = numberOfCompleters;
        this.averageScore = averageScore;
        this.groups = List.copyOf(groups);
        this.playerStats = playerStats;
    }

    /** @return per-player stats for the requesting user, or null */
    public PlayerGameStats getPlayerStats() {
        return playerStats;
    }

    /** @return unique game id */
    public int getGameId() {
        return gameId;
    }

    /** @return total number of players */
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    /** @return number of winners */
    public int getNumberOfWinners() {
        return numberOfWinners;
    }

    /** @return number of completers */
    public int getNumberOfCompleters() {
        return numberOfCompleters;
    }

    /** @return average score */
    public double getAverageScore() {
        return averageScore;
    }

    /** @return immutable list of groups */
    public List<Group> getGroups() {
        return groups;
    }

}
