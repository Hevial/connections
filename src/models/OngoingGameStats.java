package models;

/**
 * Lightweight summary of a currently running game used for monitoring.
 *
 * <p>
 * Contains high-level counters and a human-friendly time remaining
 * representation suitable for CLI display.
 * </p>
 */
public class OngoingGameStats {
    private final int gameId;
    private final String timeRemaining;
    private final int playersInProgress;
    private final int playersCompleted;
    private final int playersWon;

    /**
     * Create an ongoing game summary.
     *
     * @param gameId            unique game id
     * @param timeRemaining     human-readable remaining time (e.g., 00:12:34)
     * @param playersInProgress players currently playing
     * @param playersCompleted  players who already completed
     * @param playersWon        players who have won so far
     */
    public OngoingGameStats(int gameId, String timeRemaining, int playersInProgress, int playersCompleted,
            int playersWon) {
        this.gameId = gameId;
        this.timeRemaining = timeRemaining;
        this.playersInProgress = playersInProgress;
        this.playersCompleted = playersCompleted;
        this.playersWon = playersWon;
    }

    /** @return unique game id */
    public int getGameId() {
        return gameId;
    }

    /** @return human-readable remaining time */
    public String getTimeRemaining() {
        return timeRemaining;
    }

    /** @return number of players currently in progress */
    public int getPlayersInProgress() {
        return playersInProgress;
    }

    /** @return number of players completed */
    public int getPlayersCompleted() {
        return playersCompleted;
    }

    /** @return number of players won */
    public int getPlayersWon() {
        return playersWon;
    }

    /**
     * Returns a CLI-friendly formatted representation of the ongoing stats.
     *
     * @return formatted multi-line string
     */
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("╠ Ongoing Game Stats\n");
        sb.append("║ ").append("-".repeat(70)).append("\n");
        sb.append(String.format("║  Game ID: %d\n", gameId));
        sb.append(String.format("║  Time Remaining: %s\n", timeRemaining));
        sb.append(String.format("║  Players In Progress: %d\n", playersInProgress));
        sb.append(String.format("║  Players Completed: %d\n", playersCompleted));
        sb.append(String.format("║  Players Won: %d\n", playersWon));
        return sb.toString();
    }
}
