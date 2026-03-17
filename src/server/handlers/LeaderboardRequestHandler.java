package server.handlers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import models.LeaderboardEntry;
import models.LeaderboardReq;
import models.Response;
import models.User;
import models.UserStats;
import models.enums.Action;
import models.enums.StatusCodes;
import server.Session;
import server.db.DBManager;

public class LeaderboardRequestHandler implements RequestActionHandler {

    @Override
    public Response handle(JsonElement data, Session session) {
        Gson gson = new Gson();
        LeaderboardReq lbReq = gson.fromJson(data, LeaderboardReq.class);
        DBManager dbManager = DBManager.getInstance();
        Map<String, UserStats> allStats = dbManager.getAllUsersStats();

        List<LeaderboardEntry> leaderboard = new ArrayList<>();

        // complete leaderboard req
        if (lbReq.getTopK() == -1) {
            leaderboard = getGlobalLeaderboard(allStats, dbManager);
        } else {
            // top k leaderboard req
            leaderboard = getTopKLeaderboard(allStats, dbManager, lbReq.getTopK());
        }

        if (lbReq.getPlayerName() != null && !lbReq.getPlayerName().isBlank()) {
            leaderboard = getPlayerLeaderboardEntry(allStats, dbManager, lbReq.getPlayerName());
        }

        Type listType = new TypeToken<List<LeaderboardEntry>>() {
        }.getType();
        JsonElement resData = gson.toJsonTree(leaderboard, listType);
        return new Response(Action.LEADERBOARD, StatusCodes.SUCCESS, "Leaderboard retrieved successfully", resData);

    }

    /**
     * Builds the global leaderboard from the provided user statistics map.
     *
     * <p>
     * This method sorts all entries in {@code allStats} by
     * {@link UserStats#getScore}
     * in descending order and maps each entry to a {@link LeaderboardEntry} where
     * the username is resolved via
     * {@code dbManager.getUserById(userId).getUsername()}.
     * Rankings start at {@code 1} and increase with each entry.
     * </p>
     *
     * @param allStats  map userId -> {@link UserStats} used to build the
     *                  leaderboard
     * @param dbManager database manager used to resolve user metadata (username)
     * @return a list of {@link LeaderboardEntry} sorted by score descending with
     *         ranks
     */
    private List<LeaderboardEntry> getGlobalLeaderboard(Map<String, UserStats> allStats, DBManager dbManager) {

        List<Map.Entry<String, UserStats>> sorted = new ArrayList<>(allStats.entrySet());

        sorted.sort((a, b) -> Integer.compare(
                b.getValue().getScore(),
                a.getValue().getScore()));

        List<LeaderboardEntry> result = new ArrayList<>();

        int rank = 1;
        for (var entry : sorted) {
            String userId = entry.getKey();
            UserStats stats = entry.getValue();

            result.add(new LeaderboardEntry(
                    rank,
                    dbManager.getUserById(userId).getUsername(),
                    stats.getWinRate(),
                    stats.getScore()));

            rank++;
        }

        return result;
    }

    /**
     * Computes the top-K leaderboard using a min-heap selection algorithm.
     *
     * <p>
     * This method maintains a priority queue of size at most {@code k} to select
     * the highest scoring entries efficiently. The resulting top entries are
     * returned sorted by score in descending order. Ranks are assigned starting
     * from {@code 1} for the highest score in the returned list.
     * </p>
     *
     * @param allStats  map userId -> {@link UserStats} containing scores
     * @param dbManager database manager used to resolve usernames
     * @param k         number of top entries to return (must be > 0)
     * @return a list of up to {@code k} {@link LeaderboardEntry} sorted by score
     *         descending
     */
    private List<LeaderboardEntry> getTopKLeaderboard(
            Map<String, UserStats> allStats,
            DBManager dbManager,
            int k) {

        PriorityQueue<Map.Entry<String, UserStats>> heap = new PriorityQueue<>(
                Comparator.comparingInt(e -> e.getValue().getScore()));

        for (var entry : allStats.entrySet()) {
            heap.offer(entry);

            if (heap.size() > k) {
                heap.poll();
            }
        }

        List<Map.Entry<String, UserStats>> topEntries = new ArrayList<>(heap);

        topEntries.sort((a, b) -> Integer.compare(
                b.getValue().getScore(),
                a.getValue().getScore()));

        List<LeaderboardEntry> result = new ArrayList<>();

        int rank = 1;
        for (var entry : topEntries) {
            String userId = entry.getKey();
            UserStats stats = entry.getValue();

            result.add(new LeaderboardEntry(
                    rank,
                    dbManager.getUserById(userId).getUsername(),
                    stats.getWinRate(),
                    stats.getScore()));

            rank++;
        }

        return result;
    }

    /**
     * Returns the leaderboard entry for a single player identified by username.
     *
     * <p>
     * The method looks up the player's userId via
     * {@code dbManager.getUserByUsername}
     * and obtains their {@link UserStats} from {@code allStats}. If the player is
     * present, it computes the player's rank by counting how many users have a
     * strictly higher score, then returns a single-entry list containing the
     * corresponding {@link LeaderboardEntry}. If the player is not found, an
     * empty list is returned.
     * </p>
     *
     * @param allStats       map userId -> {@link UserStats} used to compute rank
     * @param dbManager      database manager used to resolve userId from username
     * @param targetUsername the username of the target player
     * @return a single-element list with the player's {@link LeaderboardEntry},
     *         or an empty list if the player is not found
     */
    public List<LeaderboardEntry> getPlayerLeaderboardEntry(
            Map<String, UserStats> allStats,
            DBManager dbManager,
            String targetUsername) {

        User targetUser = dbManager.getUserByUsername(targetUsername);
        if (targetUser == null) {
            return List.of(); // player non trovato
        }

        String targetUserId = targetUser.getUserId();
        UserStats stats = allStats.get(targetUserId);
        // if stats == null, player has no games played and thus no score
        if (stats == null)
            return List.of(); // player non trovato

        int targetScore = stats.getScore();
        int rank = 1;

        for (var entry : allStats.values()) {
            if (entry.getScore() > targetScore) {
                rank++;
            }
        }

        LeaderboardEntry entry = new LeaderboardEntry(
                rank,
                targetUsername,
                stats.getWinRate(),
                stats.getScore());

        return List.of(entry);
    }

}
