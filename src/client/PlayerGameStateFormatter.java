package client;

import java.util.List;

import models.Group;
import models.PlayerGameState;

/**
 * Renders {@link PlayerGameState} into a CLI-friendly board layout.
 * <p>
 * This class is intentionally UI-only: it does not mutate game state and only
 * transforms the provided model into a formatted multiline string.
 * </p>
 */
public final class PlayerGameStateFormatter {

    private static final int BOARD_COLUMNS = 4;
    private static final int CELL_WIDTH = 16;
    private static final int CONTENT_WIDTH = BOARD_COLUMNS * CELL_WIDTH + (BOARD_COLUMNS - 1);
    private static final int BORDER_WIDTH = CONTENT_WIDTH + 2;

    private PlayerGameStateFormatter() {
    }

    /**
     * Formats the provided player state into a textual game board.
     *
     * @param state player state to render
     * @return formatted multiline board string
     */
    public static String format(PlayerGameState state) {
        StringBuilder sb = new StringBuilder();

        appendBorder(sb, '╠', '═', '╗');
        appendLine(sb, "Partita #" + state.getGameId() + "  Tempo rimanente: " + state.getTimeLeft());
        appendBorder(sb, '╠', '═', '╣');
        appendCenteredLine(sb, "GRUPPI TROVATI");

        if (state.getGroupsFound().isEmpty()) {
            appendCenteredLine(sb, "Nessun gruppo trovato");
        } else {
            for (Group group : state.getGroupsFound()) {
                appendLine(sb, center(trimToWidth(group.getTheme().toUpperCase(), CONTENT_WIDTH), CONTENT_WIDTH));
                appendWordsGrid(sb, group.getWords());
                appendBorder(sb, '║', ' ', '║');
            }
        }

        appendCenteredLine(sb, "");
        appendCenteredLine(sb, "PAROLE RIMANENTI");
        if (state.getWordsLeft().isEmpty()) {
            appendCenteredLine(sb, "Board completata");
        } else {
            appendWordsGrid(sb, state.getWordsLeft());
        }

        appendBorder(sb, '╠', '═', '╣');
        appendLine(sb, "Punteggio: " + state.getScore() + "  Mistakes: " + state.getMistakes());
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
