package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a word connection game with groups of related words.
 * 
 * This class manages game data including a unique game identifier and a list
 * of groups, each containing a theme and its associated words.
 */
public class Game {

    private int gameId; // Unique identifier for the game
    private List<Group> groups; // List of word groups with their themes

    /**
     * Constructs a new Game instance with the specified id and groups.
     *
     * @param gameId unique identifier for the game
     * @param groups list of groups (themes and words) that compose the game
     */
    public Game(int gameId, List<Group> groups) {
        this.gameId = gameId;
        this.groups = groups;
    }

    /**
     * Returns the unique identifier of the game.
     *
     * @return the game id
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * Returns the list of groups for this game.
     * <p>
     * Each {@link Group} contains a theme and the words that belong to that
     * theme. The caller should not modify the returned list directly.
     * </p>
     *
     * @return the groups composing this game
     */
    public List<Group> getGroups() {
        return groups;
    }

    /**
     * Returns the theme for a proposed set of words.
     * <p>
     * A proposal is considered valid when it matches exactly one group
     * (same number of words and same word set).
     * </p>
     *
     * @param words words proposed by the player
     * @return the matching theme if the proposal is valid; {@code null} otherwise
     */
    public String getProposalTheme(Set<String> words) {
        for (Group group : groups) {
            if (group.getWords().size() == words.size() && words.containsAll(group.getWords())) {
                return group.getTheme();
            }
        }
        return null;
    }

    /**
     * Retrieves a list of all words from all groups in the game.
     *
     * @return a list containing all words from every group, the list is immutable
     */
    public List<String> getAllWords() {
        List<String> allWords = new ArrayList<>();

        for (Group group : groups) {
            allWords.addAll(group.getWords());
        }

        return List.copyOf(allWords);
    }
}