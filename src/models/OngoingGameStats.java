package models;

public class OngoingGameStats {
    private final int gameId;
    private final String timeRemaining;
    private final int playersInProgress;
    private final int playersCompleted;
    private final int playersWon;

    public OngoingGameStats(int gameId, String timeRemaining, int playersInProgress, int playersCompleted,
            int playersWon) {
        this.gameId = gameId;
        this.timeRemaining = timeRemaining;
        this.playersInProgress = playersInProgress;
        this.playersCompleted = playersCompleted;
        this.playersWon = playersWon;
    }

    public int getGameId() {
        return gameId;
    }

    public String getTimeRemaining() {
        return timeRemaining;
    }

    public int getPlayersInProgress() {
        return playersInProgress;
    }

    public int getPlayersCompleted() {
        return playersCompleted;
    }

    public int getPlayersWon() {
        return playersWon;
    }

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
