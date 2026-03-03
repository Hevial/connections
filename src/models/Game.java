package models;

import java.util.List;

public class Game {

    private int gameId;
    private List<Group> groups;

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

    public boolean isValidGroup(List<String> words) {
        for (Group group : groups) {
            if (group.getWords().containsAll(words)) {
                return true;
            }
        }
        return false;
    }
}