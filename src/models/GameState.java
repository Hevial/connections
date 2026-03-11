package models;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
    public Game setGame() {
        return game;
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
