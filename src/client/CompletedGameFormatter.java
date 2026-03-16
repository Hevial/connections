package client;

import java.util.List;

import models.CompletedGame;
import models.Group;
import models.PlayerCompletedGame;
import models.PlayerGameStats;

/**
 * Renders {@link CompletedGame} into a CLI-friendly board layout.
 * <p>
 * Uses the same visual style as PlayerGameStateFormatter: framed box,
 * centered titles and 4-column grids for group words.
 * </p>
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
     * @param game completed game to render
     * @return formatted multiline board string
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

    public static String formatForUser(PlayerCompletedGame game) {
        StringBuilder sb = new StringBuilder();

        appendBorder(sb, '╠', '═', '╗');
        appendLine(sb, "Partita #" + game.getGameId());
        appendBorder(sb, '╠', '═', '╣');
        appendCenteredLine(sb, "RISULTATI UTENTE");

        PlayerGameStats stats = game.getPlayerStats();
        if (stats == null) {
            appendCenteredLine(sb, "Nessuna statistica disponibile (perché non hai partecipato a questa partita)");
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
     * Appends the provided words as a 4-column grid.
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
     */
    private static void appendLine(StringBuilder sb, String content) {
        sb.append("║ ")
                .append(padRight(trimToWidth(content, CONTENT_WIDTH), CONTENT_WIDTH))
                .append(" ║\n");
    }

    /**
     * Appends a centered content line inside the board frame.
     */
    private static void appendCenteredLine(StringBuilder sb, String content) {
        sb.append("║ ")
                .append(center(trimToWidth(content, CONTENT_WIDTH), CONTENT_WIDTH))
                .append(" ║\n");
    }

    /**
     * Appends a horizontal border line with custom edge and fill characters.
     */
    private static void appendBorder(StringBuilder sb, char left, char fill, char right) {
        sb.append(left)
                .append(String.valueOf(fill).repeat(BORDER_WIDTH))
                .append(right)
                .append('\n');
    }

    /**
     * Centers text in a fixed-width cell.
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
     */
    private static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return text + " ".repeat(width - text.length());
    }

    /**
     * Trims text to fit the target width, using ellipsis when needed.
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
