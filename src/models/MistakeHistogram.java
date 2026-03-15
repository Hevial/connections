package models;

/**
 * Represents a histogram that tracks the number of mistakes made in games.
 * <p>
 * The histogram stores counts for games with 0 to 4 mistakes, and a special
 * bucket for games with 5 or more mistakes (considered "not finished").
 * </p>
 * <ul>
 * <li>Index 0: 0 mistakes (perfect game)</li>
 * <li>Index 1: 1 mistake</li>
 * <li>Index 2: 2 mistakes</li>
 * <li>Index 3: 3 mistakes</li>
 * <li>Index 4: 4 mistakes (game lost)</li>
 * <li>Index 5: 5 or more mistakes (not finished)</li>
 * </ul>
 */
public class MistakeHistogram {

    // Index 0: 0 mistakes (perfect game)
    // Index 1: 1 mistake
    // Index 2: 2 mistakes
    // Index 3: 3 mistakes
    // Index 4: 4 mistakes (game lost)
    // Index 5: not finished
    private final int[] histogram;

    private static final int ISTROGRAM_SIZE = 6; // 0–5 buckets (5 means not finished)
    private static final int MAX_BAR_LENGTH = 40; // Maximum width of the histogram bar

    public MistakeHistogram() {
        this.histogram = new int[ISTROGRAM_SIZE];
    }

    /**
     * Increments the counter for the bucket corresponding to the given number of
     * mistakes.
     * <p>
     * If {@code mistakes} is between 0 and {@code histogram.length - 1}, the
     * corresponding bucket is incremented. If the value is outside this range,
     * no operation is performed.
     * </p>
     *
     * @param mistakes the number of mistakes made
     *                 (0–4: specific bucket, 5: "not finished" bucket)
     */
    public void increment(int mistakes) {
        if (mistakes >= 0 && mistakes < histogram.length) {
            histogram[mistakes]++;
        }
    }

    /**
     * Returns the number of games stored in the bucket corresponding to the given
     * number of mistakes.
     * <p>
     * If {@code mistakes} is between 0 and {@code histogram.length - 1}, the count
     * of the corresponding bucket is returned. If the value is outside this range,
     * {@code -1} is returned.
     * </p>
     *
     * @param mistakes the number of mistakes
     *                 (0–4: specific bucket, 5: "not finished" bucket)
     * @return the number of games in the bucket, or {@code -1} if the index is
     *         invalid
     */
    public int getCount(int mistakes) {
        if (mistakes >= 0 && mistakes < histogram.length) {
            return histogram[mistakes];
        }
        return -1;
    }

    /**
     * Returns the total number of puzzles won.
     * <p>
     * A puzzle is considered won if it was completed with 0 to 3 mistakes.
     * </p>
     *
     * @return the number of puzzles won
     */
    public int getPuzzlesWon() {
        int won = 0;
        int MAX_WIN_MISTAKES = 3;

        for (int i = 0; i <= MAX_WIN_MISTAKES; i++) {
            won += histogram[i];
        }
        return won;
    }

    /**
     * Returns the number of puzzles lost.
     * <p>
     * A puzzle is considered lost if it was completed with 4 mistakes.
     * </p>
     *
     * @return the number of puzzles lost
     */
    public int getPuzzlesLost() {
        return histogram[4];
    }

    /**
     * Returns the number of unfinished puzzles.
     *
     * @return the number of unfinished puzzles
     */
    public int getUnfinished() {
        return histogram[5];
    }

    /**
     * Returns the total number of games recorded in the histogram.
     *
     * @return the total number of games
     */
    public int getTotal() {
        int total = 0;
        for (int count : histogram) {
            total += count;
        }
        return total;
    }

    /**
     * Returns a textual representation of the mistakes histogram scaled by
     * percentage.
     * <p>
     * For each bucket (0–4 mistakes plus "not finished"), a bar proportional to the
     * percentage of games in that bucket is displayed, along with the percentage
     * value and the absolute count.
     * </p>
     * <ul>
     * <li>Bar: proportional length representing the percentage of games</li>
     * <li>Percentage: percentage of games in the bucket</li>
     * <li>Count: absolute number of games</li>
     * </ul>
     * <p>
     * If no data is available, a dedicated message is returned.
     * </p>
     *
     * @return a formatted string representing the mistake histogram
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Mistakes histogram\n");
        sb.append("-".repeat(70)).append("\n");

        int total = 0;
        for (int count : histogram) {
            total += count;
        }

        if (total == 0) {
            sb.append("No data available.\n");
            return sb.toString();
        }

        int buckets = histogram.length;
        for (int i = 0; i < buckets; i++) {
            String label = (i < buckets - 1) ? i + " mistakes" : "not finished";
            double percent = (histogram[i] * 100.0) / total;
            int barLength = (int) Math.round((percent / 100.0) * MAX_BAR_LENGTH);
            String bar = "█".repeat(barLength);
            sb.append(
                    String.format("%-20s | %-*s | %5.1f%% (%d)\n", label, MAX_BAR_LENGTH, bar, percent, histogram[i]));
        }
        return sb.toString();
    }
}