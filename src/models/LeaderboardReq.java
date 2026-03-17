package models;

public class LeaderboardReq {

    private final int topK;
    private final String playerName;

    public LeaderboardReq(int topK, String playerName) {
        this.topK = topK;
        this.playerName = playerName;
    }

    public LeaderboardReq(int topK) {
        this(topK, null);
    }

    public LeaderboardReq(String playerName) {
        this(0, playerName);
    }

    public LeaderboardReq() {
        this(-1, null); // default all players,
    }

    public int getTopK() {
        return topK;
    }

    public String getPlayerName() {
        return playerName;
    }
}
