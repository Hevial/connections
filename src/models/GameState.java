package models;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the runtime state of a game session.
 */
public class GameState {

    private static final DateTimeFormatter CREATED_AT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter REMAINING_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public Game game;
    public long createdAt;
    public int gameDuration;

    /**
     * Creates a new game state with the given game, creation time, and duration.
     *
     * @param game         the game associated with this state
     * @param createdAt    the timestamp when the game state was created
     * @param gameDuration the total game duration in seconds
     */
    public GameState(Game game, long createdAt, int gameDuration) {
        this.game = game;
        this.createdAt = createdAt;
        this.gameDuration = gameDuration;
    }

    /**
     * Returns the game associated with this state.
     *
     * @return the current game
     */
    public Game getGame() {
        return game;
    }

    /**
     * Returns the id of the current game associated with this state.
     *
     * @return current game id
     */
    public int getGameId() {
        return game.getGameId();
    }

    /**
     * Returns a shuffled list of all words from the game.
     * <p>
     * This method retrieves all words using {@code game.getAllWords()},
     * shuffles the list in-place to randomize the order, and returns the shuffled
     * list.
     * </p>
     *
     * @return a {@code List<String>} containing all words from the game in random
     *         order
     */
    public List<String> getAllWordsShuffled() {
        List<String> shuffledWords = new ArrayList<>(game.getAllWords());
        Collections.shuffle(shuffledWords);
        return shuffledWords;
    }

    /**
     * Returns the remaining game time formatted as hours, minutes, and seconds.
     *
     * @return the remaining time in the format 00h:00m:00s
     */
    public String getRemainingTime() {
        long elapsedTime = System.currentTimeMillis() - createdAt;
        long remainingTime = Math.max(gameDuration - elapsedTime / 1000, 0);

        return LocalTime.ofSecondOfDay(remainingTime).format(REMAINING_TIME_FORMATTER);
    }

    public String getCreatedAtTime() {
        return Instant.ofEpochMilli(createdAt)
                .atZone(ZoneId.systemDefault())
                .format(CREATED_AT_FORMATTER);
    }

    public String getEndingAtTime() {
        long endingTime = createdAt + gameDuration * 1000;
        return Instant.ofEpochMilli(endingTime)
                .atZone(ZoneId.systemDefault())
                .format(CREATED_AT_FORMATTER);
    }

    public void printGameState() {
        System.out.println("Game ID: " + game.getGameId());
        System.out.println("Created At: " + getCreatedAtTime());
        System.out.println("Ending At: " + getEndingAtTime());
        System.out.println("Game Duration: " + gameDuration + "s");
        System.out.println("Remaining Time: " + getRemainingTime());

        System.out.println("Groups:");
        for (Group group : game.getGroups()) {
            System.out.println("  Theme: " + group.getTheme());
            System.out.println("  Words: " + group.getWords() + "\n");
        }
    }

}
