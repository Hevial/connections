package models;

import java.util.List;

public class PlayerCompletedGame {

    public final int gameId;
    public final int numberOfPlayers;
    public final int numberOfWinners;
    public final int numberOfCompleters;
    public final double averageScore;
    public final List<Group> groups;
    public final PlayerGameStats playerStats; // null if the player did not participate in the game

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

    public PlayerGameStats getPlayerStats() {
        return playerStats;
    }

    public int getGameId() {
        return gameId;
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

    public List<Group> getGroups() {
        return groups;
    }

}
