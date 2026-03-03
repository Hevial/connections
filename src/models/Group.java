package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class Group {

    private final String theme;
    private final Set<String> words;

    public Group(String theme, Collection<String> words) {
        this.theme = theme;
        this.words = new LinkedHashSet<>(words);
    }

    public String getTheme() {
        return theme;
    }

    public ArrayList<String> getWords() {
        return new ArrayList<>(words);
    }

    public boolean containsWord(String word) {
        return words.contains(word);
    }

}
