package models;

/**
 * Entry representing a single user in the leaderboard view.
 */
public class LeaderboardEntry {

    private final int rank;
    private final String username;
    private final double winRate;
    private final int score;

    /**
     * Create a leaderboard entry.
     *
     * @param rank     position in the leaderboard (1-based)
     * @param username player's username
     * @param winRate  player's win rate (0.0-1.0)
     * @param score    player's score
     */
    public LeaderboardEntry(int rank, String username, double winRate, int score) {
        this.rank = rank;
        this.username = username;
        this.winRate = winRate;
        this.score = score;
    }

    /** @return leaderboard rank (1-based) */
    public int getRank() {
        return rank;
    }

    /** @return username */
    public String getUsername() {
        return username;
    }

    /** @return win rate in range 0.0-1.0 */
    public double getWinRate() {
        return winRate;
    }

    /** @return player's score */
    public int getScore() {
        return score;
    }

}
