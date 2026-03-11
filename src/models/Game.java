package models;

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

    public Game(int gameId, List<Group> groups) {
        this.gameId = gameId;
        this.groups = groups;
    }

    public int getGameId() {
        return gameId;
    }

    public List<Group> getGroups() {
        return groups;
    }

    /**
     * Checks if the provided set of words forms a valid group in this game.
     *
     * @param words the set of words to validate
     * @return true if the words match exactly one of the groups, false otherwise
     */
    public boolean isValidGroup(Set<String> words) {
        for (Group group : groups) {
            if (group.getWords().size() == words.size() && words.containsAll(group.getWords())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the theme associated with the given set of words.
     *
     * @param words the set of words whose theme is to be retrieved
     * @return the theme for the given words, or null if the words do not form a
     *         valid group
     */
    public String getTheme(Set<String> words) {
        for (Group group : groups) {
            if (group.getWords().size() == words.size() && words.containsAll(group.getWords())) {
                return group.getTheme();
            }
        }
        return null;
    }
}