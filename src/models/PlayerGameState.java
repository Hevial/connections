package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public int getScore() {
        return score;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

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

    public List<String> getWordsLeft() {
        return wordsLeft == null ? List.of() : List.copyOf(wordsLeft);
    }

    public String getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(String timeLeft) {
        this.timeLeft = timeLeft;
    }

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

    public boolean isLoser() {
        return isLoser;
    }

    public void setLoser(boolean isLoser) {
        this.isLoser = isLoser;
    }

}
