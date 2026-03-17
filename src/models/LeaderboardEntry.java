package models;

public class LeaderboardEntry {

    private final int rank;
    private final String username;
    private final double winRate;
    private final int score;

    public LeaderboardEntry(int rank, String username, double winRate, int score) {
        this.rank = rank;
        this.username = username;
        this.winRate = winRate;
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public String getUsername() {
        return username;
    }

    public double getWinRate() {
        return winRate;
    }

    public int getScore() {
        return score;
    }

}
