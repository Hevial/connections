package models;

import java.util.List;
import java.util.Map;

public class PlayerGameState {

    private static final int POINTS_PER_SUCCESS = 6;
    private static final int POINTS_PER_MISTAKE = 4;

    private int mistakes;
    private int score;
    private boolean hasWon;
    private Map<String, Group> groupsFound;

    public PlayerGameState() {
        this.mistakes = 0;
        this.score = 0;
        this.hasWon = false;
    }

    public int getMistakes() {
        return mistakes;
    }

    public int getScore() {
        return score;
    }

    public boolean isHasWon() {
        return hasWon;
    }

    public void setHasWon(boolean hasWon) {
        this.hasWon = hasWon;
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

    public List<Group> getGroupsFound() {
        return List.copyOf(groupsFound.values());
    }

    public void addGroupFound(Group group) {
        groupsFound.put(group.getTheme(), group);
    }

}
