package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mutable runtime representation of a single player's state within an active
 * game session.
 *
 * <p>
 * This class tracks the player's progress, including mistakes, score,
 * found groups, remaining words, and whether the player has completed or won
 * the game. It exposes methods to update state when the player submits a
 * proposal and to query validity and membership of proposed words.
 * </p>
 *
 * <p>
 * Instances are intended to be used by a single-threaded game loop or
 * by well-synchronized server code; no internal synchronization is
 * provided.
 * </p>
 */
public class PlayerGameState {

    private static final int POINTS_PER_SUCCESS = 6;
    private static final int POINTS_PER_MISTAKE = 4;
    private static final int LOSING_MISTAKES = 4;

    private int gameId;
    private int mistakes;
    private int score;
    private boolean isWinner;
    private boolean isCompleted;
    private boolean isLoser;
    private List<Group> groupsFound;
    private List<Group> groupsLeft;
    private List<String> wordsLeft; // initially contains all words, then updated by removing found words
    private Set<String> allWords;
    private String timeLeft;

    public PlayerGameState(int gameId, List<String> wordsLeft, String timeLeft, List<Group> groupsLeft) {
        this.gameId = gameId;
        this.mistakes = 0;
        this.score = 0;
        this.isWinner = false;
        this.isCompleted = false;
        this.groupsFound = new ArrayList<>();
        this.groupsLeft = new ArrayList<>(groupsLeft);
        this.wordsLeft = new ArrayList<>(wordsLeft);
        this.allWords = new HashSet<>(wordsLeft);
        this.timeLeft = timeLeft;
    }

    /**
     * Returns the number of mistakes made by the player in the current game.
     * <p>
     * This value increases each time the player makes an incorrect proposal.
     * </p>
     *
     * @return the number of mistakes
     */
    public int getMistakes() {
        return mistakes;
    }

    /**
     * Returns the current score for the player in this game.
     *
     * @return the player's score (may be negative if penalties were applied)
     */
    public int getScore() {
        return score;
    }

    /**
     * Indicates whether the player has completed the game as a winner.
     *
     * @return {@code true} if the player won the game; {@code false} otherwise
     */
    public boolean isWinner() {
        return isWinner;
    }

    /**
     * Indicates whether the player's game session is finished
     * (either by winning or losing, otherwise is not completed).
     *
     * @return {@code true} when the player's session is complete; {@code false}
     *         when it is still active
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Marks the player's session as complete or incomplete.
     *
     * @param isCompleted {@code true} to mark the session complete; {@code
     *                    false} to mark it active
     */
    public void setComplete(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    /**
     * Increments the mistake counter by one.
     * <p>
     * When the number of mistakes reaches {@code LOSING_MISTAKES}, the player is
     * marked as loser.
     * </p>
     */
    public void incrementMistakes() {
        mistakes++;
        isLoser = mistakes >= LOSING_MISTAKES;
        isCompleted = isLoser; // Game is complete if the player has lost
    }

    /**
     * Increments the player's score by 6 points.
     * This method is typically called when a player completes a successful action
     * or achieves a game objective.
     */
    public void incrementScore() {
        score += POINTS_PER_SUCCESS;
    }

    /**
     * Decrements the player's score by 4 points.
     * This method is typically called when a player makes an incorrect move or
     * action
     * that results in a penalty.
     */
    public void decrementScore() {
        score -= POINTS_PER_MISTAKE;
    }

    /**
     * Returns the list of groups correctly found by the player during the current
     * game.
     * <p>
     * Each group represents a correct proposal made by the player.
     * The returned list is immutable.
     * </p>
     *
     * @return an immutable list of groups found by the player
     */
    public List<Group> getGroupsFound() {
        return groupsFound == null ? List.of() : List.copyOf(groupsFound);
    }

    /**
     * Marks a proposal as found and updates the live player state.
     * <p>
     * The method tries to find an equivalent group inside {@code groupsLeft}
     * (set-based comparison, word order ignored). If found, that original group is
     * moved to {@code groupsFound}; otherwise a fallback group is created from the
     * provided theme and words.
     * </p>
     * <p>
     * After the group update, proposal words are removed from {@code wordsLeft}.
     * When no words remain, the player is marked as winner and the game as
     * complete.
     * </p>
     *
     * @param theme theme associated with the found group
     * @param words words proposed by the player
     */
    public void addFoundGroup(String theme, Set<String> words) {
        Set<String> proposalWords = Set.copyOf(words);
        Group foundGroup = null;

        for (Group group : groupsLeft) {
            if (Set.copyOf(group.getWords()).equals(proposalWords)) {
                foundGroup = group;
                break;
            }
        }

        if (foundGroup != null) {
            groupsLeft.remove(foundGroup);
            groupsFound.add(foundGroup);
        } else {
            // Fallback in case groupsLeft is out of sync for any reason.
            groupsFound.add(new Group(theme, new ArrayList<>(proposalWords)));
            System.err.println(
                    "Warning: Group not found in groupsLeft for theme '" + theme + "' and words " + proposalWords);
        }

        wordsLeft.removeAll(words);

        if (wordsLeft.isEmpty()) {
            isCompleted = true;
            isWinner = true;
        }
    }

    /**
     * Returns a read-only view of the words that the player has not yet found.
     *
     * @return an immutable list of remaining words for this player
     */
    public List<String> getWordsLeft() {
        return wordsLeft == null ? List.of() : List.copyOf(wordsLeft);
    }

    /**
     * Returns the human-readable remaining time for the player's session.
     *
     * @return the remaining time string as produced by the game loop
     */
    public String getTimeLeft() {
        return timeLeft;
    }

    /**
     * Sets the human-readable remaining time for the player's session.
     *
     * @param timeLeft formatted remaining time (display string)
     */
    public void setTimeLeft(String timeLeft) {
        this.timeLeft = timeLeft;
    }

    /**
     * Returns the identifier of the game this player state belongs to.
     *
     * @return the game id
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * Checks whether a proposed group has already been found by the player.
     * <p>
     * The comparison is set-based, so word order does not matter.
     * </p>
     *
     * @param words the proposed group words
     * @return {@code true} if an equivalent group is already present in
     *         {@code groupsFound}; {@code false} otherwise
     */
    public boolean hasFoundGroup(Set<String> words) {
        Set<String> proposalWords = Set.copyOf(words);
        for (Group foundGroup : groupsFound) {
            if (Set.copyOf(foundGroup.getWords()).equals(proposalWords)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates that all proposed words belong to the current game dictionary.
     * <p>
     * This check does not evaluate whether words form a correct group; it only
     * verifies membership against the complete set of game words.
     * </p>
     *
     * @param words proposed words to validate
     * @return {@code true} if all words are part of this game; {@code false}
     *         otherwise
     */
    public boolean areWordsValid(Set<String> words) {
        return allWords.containsAll(words);
    }

    /**
     * Indicates whether the player has lost the game (reached the mistake
     * threshold).
     *
     * @return {@code true} if the player is a loser; {@code false} otherwise
     */
    public boolean isLoser() {
        return isLoser;
    }

    /**
     * Manually sets the loser flag for the player. Normally the
     * flag is managed by {@link #incrementMistakes()}.
     *
     * @param isLoser {@code true} to mark the player as loser; {@code false}
     *                otherwise
     */
    public void setLoser(boolean isLoser) {
        this.isLoser = isLoser;
    }

}
