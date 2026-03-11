package models;

import java.util.Map;
import java.util.Set;

public class PlayerGameState {

    private static final int POINTS_PER_SUCCESS = 6;
    private static final int POINTS_PER_MISTAKE = 4;

    private int mistakes;
    private int score;
    private boolean isWinner;
    private boolean isComplete;
    private Map<Set<String>, String> groupsFound;

    public PlayerGameState() {
        this.mistakes = 0;
        this.score = 0;
        this.isWinner = false;
        this.isComplete = false;
        this.groupsFound = Map.of(); // Initialize with an empty map
    }

    public int getMistakes() {
        return mistakes;
    }

    public int getScore() {
        return score;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean isWinner) {
        this.isWinner = isWinner;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    /**
     * Increments the mistake counter by one.
     * This method is used to track the number of mistakes made by the player
     * during gameplay.
     */
    public void incrementMistakes() {
        mistakes++;
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

    public Map<Set<String>, String> getGroupsFound() {
        return Map.copyOf(groupsFound);
    }

    public void addGroupFound(String theme, Set<String> words) {
        groupsFound.put(words, theme);
    }

}
