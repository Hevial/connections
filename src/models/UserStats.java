package models;

public class UserStats {

    /*
     * The total number of puzzles completed,
     * including those solved in under 4 mistakes, those failed, and those not
     * finished.
     */
    private int puzzlesCompleted;
    private double winRate;
    private double lossRate;
    private int currentStreak;
    private int maxStreak;
    private int perfectPuzzles;
    private MistakeHistogram mistakeHistogram; // Index 0: 1 mistake, Index 1: 2 mistakes, ..., Index 3: 4 mistakes,
                                               // Index
                                               // 4: not finished

    public UserStats(int puzzlesCompleted, int currentStreak,
            int maxStreak, int perfectPuzzles, MistakeHistogram mistakeHistogram) {
        this.puzzlesCompleted = puzzlesCompleted;
        this.currentStreak = currentStreak;
        this.maxStreak = maxStreak;
        this.perfectPuzzles = perfectPuzzles;
        this.mistakeHistogram = mistakeHistogram;

        this.winRate = calculateWinRate();
        this.lossRate = calculateLossRate();
    }

    public void setPuzzlesCompleted(int puzzlesCompleted) {
        this.puzzlesCompleted = puzzlesCompleted;
    }

    public void incrementPuzzlesCompleted() {
        this.puzzlesCompleted++;
    }

    /**
     * Increments the current streak counter by one.
     * If the new current streak exceeds the maximum streak,
     * updates the maximum streak to match the current streak.
     */
    public void incrementCurrentStreak() {
        this.currentStreak++;
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak;
        }
    }

    public void incrementPerfectPuzzles() {
        this.perfectPuzzles++;
    }

    public void resetCurrentStreak() {
        this.currentStreak = 0;
    }

    /**
     * Records a mistake by incrementing the mistake histogram with the specified
     * number of mistakes.
     * Also recalculates the win and loss rates after recording the mistake.
     *
     * @param mistakes the number of mistakes to record
     * @see MistakeHistogram#increment(int)
     * @see #calculateWinRate()
     * @see #calculateLossRate()
     */
    public void recordMistake(int mistakes) {
        this.mistakeHistogram.increment(mistakes);
        this.winRate = calculateWinRate();
        this.lossRate = calculateLossRate();
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public void setLossRate(double lossRate) {
        this.lossRate = lossRate;
    }

    public int getPuzzlesCompleted() {
        return puzzlesCompleted;
    }

    public double getWinRate() {
        return winRate;
    }

    public double getLossRate() {
        return lossRate;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public int getPerfectPuzzles() {
        return perfectPuzzles;
    }

    public MistakeHistogram getMistakeHistogram() {
        return mistakeHistogram;
    }

    /**
     * Calculates the win rate based on the number of puzzles won and the total
     * number of puzzles attempted.
     * The win rate is computed as the ratio of puzzles won to the total puzzles.
     * If no puzzles have been attempted, the win rate is 0.0.
     *
     * @return the win rate as a double value between 0.0 and 1.0
     */
    public double calculateWinRate() {
        int puzzlesWon = mistakeHistogram.getPuzzlesWon();
        int total = mistakeHistogram.getTotal();

        return total > 0 ? (double) puzzlesWon / total : 0.0;
    }

    /**
     * Calculates the loss rate for the user based on the mistake histogram.
     * The loss rate is defined as the ratio of puzzles lost to the total number of
     * puzzles attempted.
     * If no puzzles have been attempted, the loss rate is 0.0.
     *
     * @return the loss rate as a double value between 0.0 and 1.0
     */
    public double calculateLossRate() {
        int puzzlesLost = mistakeHistogram.getPuzzlesLost();
        int total = mistakeHistogram.getTotal();

        return total > 0 ? (double) puzzlesLost / total : 0.0;
    }

    /**
     * Builds a CLI-friendly multi-line summary of the user's statistics.
     *
     * <p>
     * The resulting string contains a header and a separator line followed by
     * lines showing: total puzzles completed, win rate (as a percentage), loss
     * rate (as a percentage), current streak, maximum streak, and number of
     * perfect puzzles. The method then appends the formatted mistake histogram
     * produced by {@code mistakeHistogram.toFormattedString()}.
     * </p>
     *
     * <p>
     * The string is intended for terminal display and includes box-drawing
     * characters used throughout the CLI UI. The method never returns
     * {@code null} (it always returns a non-empty string).
     * </p>
     *
     * @return a multi-line {@link String} containing the formatted user
     *         statistics for CLI display
     */
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();

        sb.append("╠══════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                              USER STATS                              ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════════════╣\n");

        sb.append(String.format("║ %-30s %37d ║\n", "Puzzles completed:", puzzlesCompleted));
        sb.append(String.format("║ %-30s %36.2f%% ║\n", "Win rate:", winRate * 100));
        sb.append(String.format("║ %-30s %36.2f%% ║\n", "Loss rate:", lossRate * 100));
        sb.append(String.format("║ %-30s %37d ║\n", "Current streak:", currentStreak));
        sb.append(String.format("║ %-30s %37d ║\n", "Max streak:", maxStreak));
        sb.append(String.format("║ %-30s %37d ║\n", "Perfect puzzles:", perfectPuzzles));

        sb.append("╠══════════════════════════════════════════════════════════════════════╣\n");
        sb.append(mistakeHistogram.toFormattedString());
        sb.append("╚══════════════════════════════════════════════════════════════════════╝\n");

        return sb.toString();
    }

}
