package models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerGameState {

    private static final int POINTS_PER_SUCCESS = 6;
    private static final int POINTS_PER_MISTAKE = 4;

    private int gameId;
    private int mistakes;
    private int score;
    private boolean isWinner;
    private boolean isComplete;
    private Map<Set<String>, String> groupsFound;
    private List<String> wordsLeft;
    private String timeLeft;

    public PlayerGameState(int gameId, List<String> wordsLeft, String timeLeft) {
        this.gameId = gameId;
        this.mistakes = 0;
        this.score = 0;
        this.isWinner = false;
        this.isComplete = false;
        this.groupsFound = new LinkedHashMap<>();
        this.wordsLeft = new ArrayList<>(wordsLeft);
        this.timeLeft = timeLeft;
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
        groupsFound.put(Set.copyOf(words), theme);
    }

    public List<String> getWordsLeft() {
        return List.copyOf(wordsLeft);
    }

    public void setWordsLeft(List<String> wordsLeft) {
        this.wordsLeft = new ArrayList<>(wordsLeft);
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

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

}
