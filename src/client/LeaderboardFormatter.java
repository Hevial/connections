package client;

import models.LeaderboardEntry;

import java.util.List;

/**
 * Utility class to format a leaderboard as a CLI-friendly multi-line string.
 *
 * <p>The formatter produces a fixed-width table using box-drawing
 * characters. It truncates long usernames and formats numeric columns with
 * reasonable column widths to produce readable output on typical terminals.</p>
 *
 * @see models.LeaderboardEntry
 */
public class LeaderboardFormatter {

    /** Width of the rank column (characters). */
    private static final int RANK_WIDTH = 4;
    /** Width of the username column (characters). */
    private static final int USERNAME_WIDTH = 20;
    /** Width of the score column (characters). */
    private static final int SCORE_WIDTH = 6;
    /** Width of the win-rate column (characters), e.g. "100.00%". */
    private static final int WINRATE_WIDTH = 6;
    /** Total table content width including paddings and separators. */
    private static final int TOTAL_WIDTH = RANK_WIDTH + USERNAME_WIDTH + SCORE_WIDTH + WINRATE_WIDTH + 12; // 12 for borders and spacing

    /**
     * Formats a list of {@link LeaderboardEntry} into a neat string table.
     *
     * <p>The provided list is assumed to be pre-sorted in descending order by
     * score. The method handles null or empty lists by emitting a single row
     * with a suitable message. Usernames longer than {@code USERNAME_WIDTH}
     * are trimmed and suffixed with an ellipsis.</p>
     *
     * @param entries the list of leaderboard entries (may be {@code null} or empty)
     * @return a formatted multi-line string representing the leaderboard
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