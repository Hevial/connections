package models;

/**
 * PlayerGameStats holds the per-player summary for a single completed game.
 * <p>
 * It contains the number of correct proposals made by the player, the number
 * of mistakes, the final score and a derived result string indicating whether
 * the player won, lost or the game was left incomplete for that player.
 * </p>
 */
public class PlayerGameStats {

    private int correctProposals;
    private int mistakes;
    private int score;
    private String result; // "win", "lose", "incomplete"

    /**
     * Creates a new PlayerGameStats instance.
     * <p>
     * The {@code result} field is derived from the provided values:
     * <ul>
     * <li>"win" when {@code correctProposals} &gt;= 4</li>
     * <li>"lose" when {@code mistakes} &gt;= 4</li>
     * <li>"incomplete" otherwise</li>
     * </ul>
     * </p>
     *
     * @param correctProposals number of correct proposals found by the player
     * @param mistakes         number of mistakes made by the player
     * @param score            final score obtained by the player
     */
    public PlayerGameStats(int correctProposals, int mistakes, int score) {
        this.correctProposals = correctProposals;
        this.mistakes = mistakes;
        this.score = score;
        if (correctProposals >= 4) {
            this.result = "win";
        } else if (mistakes >= 4) {
            this.result = "lose";
        } else {
            this.result = "incomplete";
        }
    }

    /**
     * Returns the number of correct proposals (groups) found by the player.
     *
     * @return the number of correct proposals
     */
    public int getCorrectProposals() {
        return correctProposals;
    }

    /**
     * Returns the number of mistakes made by the player during the game.
     *
     * @return the number of mistakes
     */
    public int getMistakes() {
        return mistakes;
    }

    /**
     * Returns the final score obtained by the player in the game.
     *
     * @return the final score
     */
    public int getScore() {
        return score;
    }

    /**
     * Returns the derived result for the player in this game.
     * <p>
     * Possible values: "win", "lose", "incomplete".
     * </p>
     *
     * @return the result string
     */
    public String getResult() {
        return result;
    }

}
