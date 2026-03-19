package client;

import java.util.List;

import models.CompletedGame;
import models.Group;
import models.PlayerCompletedGame;
import models.PlayerGameStats;

/**
 * Utility class to render {@link CompletedGame} instances as plain-text
 *, terminal-friendly boards and summaries.
 *
 * <p>Output is formatted using box-drawing characters and fixed-width cells
 * to produce a compact, human-readable representation suitable for CLI
 * display. The formatter produces full game boards, per-user views and
 * compact summaries.</p>
 *
 * <p>Formatting constants (column count, cell width and content width) are
 * tuned for an 80-column terminal but can be adjusted if necessary.</p>
 *
 * @see models.CompletedGame
 */
public final class CompletedGameFormatter {

    private static final int BOARD_COLUMNS = 4;
    private static final int CELL_WIDTH = 16;
    private static final int CONTENT_WIDTH = BOARD_COLUMNS * CELL_WIDTH + (BOARD_COLUMNS - 1);
    private static final int BORDER_WIDTH = CONTENT_WIDTH + 2;

    private CompletedGameFormatter() {
    }

    /**
     * Formats the provided completed game into a textual summary board.
     *
        * <p>The returned string contains a framed board with game metadata,
        * winners and groups laid out in a 4-column grid. The content may
        * include internationalized strings as provided by the {@link CompletedGame}
        * model.</p>
        *
        * @param game completed game to render; must not be {@code null}
        * @return formatted multi-line board string suitable for console output
     */
    public static String format(CompletedGame game) {
        StringBuilder sb = new StringBuilder();

        appendBorder(sb, '╠', '═', '╗');
        appendLine(sb, "Partita #" + game.getGameId() + "  Giocatori: " + game.getNumberOfPlayers());
        appendBorder(sb, '╠', '═', '╣');
        appendCenteredLine(sb, "RISULTATI PARTITA");

        appendLine(sb, "Vincitori: " + game.getNumberOfWinners() + "  Completate: " + game.getNumberOfCompleters());
        appendLine(sb, "Punteggio medio: " + String.format("%.2f", game.getAverageScore()));

        appendBorder(sb, '╠', '═', '╣');
        appendCenteredLine(sb, "GRUPPI (SOLUZIONE)");

        if (game.getGroups().isEmpty()) {
            appendCenteredLine(sb, "Nessun gruppo disponibile");
        } else {
            for (Group group : game.getGroups()) {
                appendLine(sb, center(trimToWidth(group.getTheme().toUpperCase(), CONTENT_WIDTH), CONTENT_WIDTH));
                appendWordsGrid(sb, group.getWords());
                appendBorder(sb, '║', ' ', '║');
            }
        }

        appendBorder(sb, '╠', '═', '╝');
        return sb.toString();
    }

    
    /**
     * Formats a per-user view of a completed game.
     *
     * <p>This view contains the user's statistics (if present) and the
     * solution groups. When the {@link PlayerCompletedGame#getPlayerStats()}
     * is {@code null} the user did not participate and a suitable message is
     * shown.</p>
     *
     * @param game the player-specific completed game view; must not be {@code null}
     * @return formatted multi-line string for the specified player
     */
    public static String formatForUser(PlayerCompletedGame game) {
        StringBuilder sb = new StringBuilder();

        appendBorder(sb, '╠', '═', '╗');
        appendLine(sb, "Partita #" + game.getGameId());
        appendBorder(sb, '╠', '═', '╣');
        appendCenteredLine(sb, "RISULTATI UTENTE");

        PlayerGameStats stats = game.getPlayerStats();
        if (stats == null) {
            appendCenteredLine(sb, "Nessuna statistica disponibile (non partecipante)");
        } else {
            appendLine(sb, "Proposte corrette: " + stats.getCorrectProposals() + "  Errori: " + stats.getMistakes());
            appendLine(sb, "Punteggio: " + stats.getScore() + "  Esito: " + stats.getResult());
        }

        appendBorder(sb, '╠', '═', '╣');
        appendCenteredLine(sb, "GRUPPI (SOLUZIONE)");

        if (game.getGroups().isEmpty()) {
            appendCenteredLine(sb, "Nessun gruppo disponibile");
        } else {
            for (Group group : game.getGroups()) {
                appendLine(sb, center(trimToWidth(group.getTheme().toUpperCase(), CONTENT_WIDTH), CONTENT_WIDTH));
                appendWordsGrid(sb, group.getWords());
                appendBorder(sb, '║', ' ', '║');
            }
        }

        appendBorder(sb, '╠', '═', '╝');
        return sb.toString();
    }

    /**
     * Builds a compact, CLI-friendly summary of a completed game.
     *
     * <p>
     * The summary contains a header, a separator line and a small set of
     * aggregate statistics: game id, total number of players, number of
     * winners, number of players who completed the game and the average score.
     * The output uses box-drawing characters and is intended for direct
     * printing to a terminal.
     * </p>
     *
    * @param game the {@link CompletedGame} instance to summarize; must not be {@code null}
    * @return a multi-line {@link String} containing the formatted summary
     */
    public static String formatSummary(CompletedGame game) {
        StringBuilder sb = new StringBuilder();
        sb.append("╠ Statistiche Partita").append("\n");
        sb.append("║ ").append("-".repeat(70)).append("\n");
        sb.append("║  Partita #" + game.getGameId()).append("\n");
        sb.append("║  Giocatori: " + game.getNumberOfPlayers()).append("\n");
        sb.append("║  Vincitori: " + game.getNumberOfWinners()).append("\n");
        sb.append("║  Completate: " + game.getNumberOfCompleters()).append("\n");
        sb.append("║  Punteggio medio: " + String.format("%.2f", game.getAverageScore())).append("\n");
        return sb.toString();
    }

    /**
     * Appends the provided words as a 4-column grid.
     *
     * @param sb destination string builder
     * @param words list of words to format (may be empty)
     */
    private static void appendWordsGrid(StringBuilder sb, List<String> words) {
        for (int index = 0; index < words.size(); index += BOARD_COLUMNS) {
            StringBuilder row = new StringBuilder();
            for (int column = 0; column < BOARD_COLUMNS; column++) {
                if (column > 0) {
                    row.append(" ");
                }

                int wordIndex = index + column;
                String word = wordIndex < words.size() ? words.get(wordIndex).toUpperCase() : "";
                row.append(center(trimToWidth(word, CELL_WIDTH), CELL_WIDTH));
            }
            appendLine(sb, row.toString());
        }
    }

    /**
     * Appends a left-aligned content line inside the board frame.
     *
     * @param sb destination builder
     * @param content text to place in the line
     */
    private static void appendLine(StringBuilder sb, String content) {
        sb.append("║ ")
                .append(padRight(trimToWidth(content, CONTENT_WIDTH), CONTENT_WIDTH))
                .append(" ║\n");
    }

    /**
     * Appends a centered content line inside the board frame.
     *
     * @param sb destination builder
     * @param content text to center
     */
    private static void appendCenteredLine(StringBuilder sb, String content) {
        sb.append("║ ")
                .append(center(trimToWidth(content, CONTENT_WIDTH), CONTENT_WIDTH))
                .append(" ║\n");
    }

    /**
     * Appends a horizontal border line with custom edge and fill characters.
     *
     * @param sb destination builder
     * @param left left edge character
     * @param fill fill character repeated across the content width
     * @param right right edge character
     */
    private static void appendBorder(StringBuilder sb, char left, char fill, char right) {
        sb.append(left)
                .append(String.valueOf(fill).repeat(BORDER_WIDTH))
                .append(right)
                .append('\n');
    }

    /**
     * Centers text in a fixed-width cell.
     *
     * @param text the text to center
     * @param width the target cell width
     * @return a string of length {@code width} with {@code text} centered
     */
    private static String center(String text, int width) {
        if (text.length() >= width) {
            return text;
        }

        int totalPadding = width - text.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    /**
     * Pads text with spaces on the right up to the target width.
     *
     * @param text text to pad
     * @param width target width
     * @return padded string
     */
    private static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return text + " ".repeat(width - text.length());
    }

    /**
     * Trims text to fit the target width, using ellipsis when needed.
     *
     * @param text input text
     * @param width maximum allowed width
     * @return possibly trimmed text
     */
    private static String trimToWidth(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        if (width <= 1) {
            return text.substring(0, width);
        }
        return text.substring(0, width - 3) + "...";
    }

}
