package models;

import java.util.List;

/**
 * Represents a group of related words associated with a theme.
 */
public class Group {

    private String theme;
    private List<String> words;

    /**
     * Creates a group with the given theme and words.
     *
     * @param theme the theme associated with the group
     * @param words the words that belong to the group
     */
    public Group(String theme, List<String> words) {
        this.theme = theme;
        this.words = words;
    }

    /**
     * Returns the theme associated with this group.
     *
     * @return the group theme
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Returns the words contained in this group.
     *
     * @return the list of group words
     */
    public List<String> getWords() {
        return words;
    }
}
