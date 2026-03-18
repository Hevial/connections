package client;

import models.LeaderboardEntry;

import java.util.List;

/**
 * Utility class to format a leaderboard as a string (CLI-friendly).
 */
public class LeaderboardFormatter {

    private static final int RANK_WIDTH = 4;
    private static final int USERNAME_WIDTH = 20;
    private static final int SCORE_WIDTH = 6;
    private static final int WINRATE_WIDTH = 6; // abbastanza per "100.00%"
    private static final int TOTAL_WIDTH = RANK_WIDTH + USERNAME_WIDTH + SCORE_WIDTH + WINRATE_WIDTH + 12; // 12 for
                                                                                                           // borders
                                                                                                           // and
                                                                                                           // spacing

    /**
     * Formats a list of LeaderboardEntry into a neat string representation.
     *
     * @param entries the list of leaderboard entries (already sorted by score
     *                descending)
     * @return formatted leaderboard as string
     */
    public static String format(List<LeaderboardEntry> entries) {
        StringBuilder sb = new StringBuilder();

        // Header top
        sb.append("╠").append("═".repeat(TOTAL_WIDTH)).append("╗\n");

        // Column titles
        sb.append(String.format("║ %-" + RANK_WIDTH + "s │ %-" + USERNAME_WIDTH + "s │ %"
                + SCORE_WIDTH + "s │  %" + WINRATE_WIDTH + "s ║%n",
                "Rank", "Username", "Score", "Win%"));

        // Separator
        sb.append("║").append("─".repeat(TOTAL_WIDTH)).append("║\n");

        // Content
        if (entries == null || entries.isEmpty()) {
            String emptyMessage = "No entries available";
            sb.append(String.format("║ %-" + RANK_WIDTH + "s │ %-" + USERNAME_WIDTH + "s │ %"
                    + SCORE_WIDTH + "s │ %" + WINRATE_WIDTH + "s  ║%n",
                    "-", emptyMessage, "-", "-"));
        } else {
            for (LeaderboardEntry entry : entries) {
                String trimmedUsername = entry.getUsername().length() > USERNAME_WIDTH
                        ? entry.getUsername().substring(0, USERNAME_WIDTH - 3) + "..."
                        : entry.getUsername();

                sb.append(String.format("║ %-" + RANK_WIDTH + "d │ %-" + USERNAME_WIDTH + "s │ %"
                        + SCORE_WIDTH + "d │ %" + WINRATE_WIDTH + ".2f%% ║%n",
                        entry.getRank(),
                        trimmedUsername,
                        entry.getScore(),
                        entry.getWinRate() * 100));
            }
        }

        // Footer
        sb.append("╠").append("═".repeat(TOTAL_WIDTH)).append("╝\n");

        return sb.toString();
    }
}