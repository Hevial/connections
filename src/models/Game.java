package models;

import java.util.Map;
import java.util.Set;

/**
 * Represents a word connection game with groups of related words.
 * 
 * This class manages game data including a unique game identifier and a mapping
 * of word groups to their corresponding themes. Each group is represented as a
 * set of words that share a common theme or category.
 */
public class Game {

    private int gameId; // Unique identifier for the game
    private Map<Set<String>, String> groups; // Mapping of word groups to their themes
    private int createdAt; // Timestamp of when the game was created

    /**
     * Constructs a Game with the specified game ID and word groups.
     *
     * @param gameId the unique identifier for this game
     * @param groups a map where each set of words is associated with a theme
     */
    public Game(int gameId, Map<Set<String>, String> groups) {
        this.gameId = gameId;
        this.groups = groups;
        this.createdAt = (int) System.currentTimeMillis();
    }

    /**
     * Retrieves the unique identifier of this game.
     *
     * @return the game ID
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * Checks if the provided set of words forms a valid group in this game.
     *
     * @param words the set of words to validate
     * @return true if the words form a valid group, false otherwise
     */
    public boolean isValidGroup(Set<String> words) {
        return groups.containsKey(words);
    }

    /**
     * Retrieves the theme associated with the given set of words.
     *
     * @param words the set of words whose theme is to be retrieved
     * @return the theme for the given words, or null if the words do not form a
     *         valid group
     */
    public String getTheme(Set<String> words) {
        return groups.get(words);
    }

    /**
     * Retrieves the timestamp when this game was created.
     *
     * @return the creation timestamp in milliseconds
     */
    public int getCreatedAt() {
        return createdAt;
    }
}