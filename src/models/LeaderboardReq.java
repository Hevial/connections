package models;

/**
 * Request object for leaderboard queries.
 *
 * <p>
 * Supports fetching the top-K players or the stats for a single player by
 * name. When {@code topK} is negative the request is interpreted as "all
 * players".
 * </p>
 */
public class LeaderboardReq {

    private final int topK;
    private final String playerName;

    /**
     * Create a leaderboard request with both topK and playerName options.
     *
     * @param topK       number of top players to request (negative for all)
     * @param playerName optional specific player to query
     */
    public LeaderboardReq(int topK, String playerName) {
        this.topK = topK;
        this.playerName = playerName;
    }

    /** @param topK request top-K players */
    public LeaderboardReq(int topK) {
        this(topK, null);
    }

    /** @param playerName request leaderboard entry for this player */
    public LeaderboardReq(String playerName) {
        this(0, playerName);
    }

    /** Default: all players. */
    public LeaderboardReq() {
        this(-1, null); // default all players,
    }

    /** @return requested top-K or negative for all players */
    public int getTopK() {
        return topK;
    }

    /** @return optional player name filter, or null */
    public String getPlayerName() {
        return playerName;
    }
}
