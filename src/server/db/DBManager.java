package server.db;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import models.CompletedGame;
import models.Game;
import models.MistakeHistogram;
import models.PlayerGameState;
import models.User;
import models.UserStats;

/**
 * DBManager is a singleton class responsible for managing user and game data in
 * the application.
 * <p>
 * It provides thread-safe operations for user authentication, registration,
 * credential updates,
 * and session management, as well as for loading and saving user data to
 * persistent storage.
 * The class also supports streaming access to game data from a JSON file.
 * </p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 * <li>Maintains an in-memory cache of users and their login states.</li>
 * <li>Handles user registration, authentication, and credential updates.</li>
 * <li>Persists user data to and loads user data from a JSON file.</li>
 * <li>Provides streaming access to game data from a JSON file.</li>
 * <li>Ensures thread-safe access and modification of user and game data.</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * 
 * <pre>
 * DBManager dbManager = DBManager.getInstance();
 * DBStatus status = dbManager.loginUser("username", "password");
 * // ... other operations
 * </pre>
 *
 * <h2>Thread Safety:</h2>
 * <p>
 * Public methods are thread-safe, using synchronization or concurrent
 * collections
 * to ensure data consistency in a multi-threaded environment.
 * </p>
 *
 * <h2>Persistence:</h2>
 * <p>
 * User data is persisted as JSON to a file specified in the configuration.
 * Game data is read from a JSON file using a streaming reader for efficiency.
 * </p>
 *
 */

// TODO: Refactor to separete responsibilities user management and game data
// management into different classes if needed.
// (Maybe)
public class DBManager {

    private static DBManager instance;
    private DBConfig config;
    private ConcurrentHashMap<String, User> usersCache; // key: userId, value: User
    private ConcurrentHashMap<String, String> usernameToId; // key: username, value: userId
    private Set<String> loggedInUsers; // Set of userIds representing currently logged-in users
    private Gson gson;
    private JsonReader gamesJsonReader; // Used for streaming read of games

    private DBManager() {
        try {
            usersCache = new ConcurrentHashMap<>();
            usernameToId = new ConcurrentHashMap<>();
            loggedInUsers = ConcurrentHashMap.newKeySet();
            gson = new Gson();
            config = DBConfig.loadConfig();
            loadUsers();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    /**
     * Returns the singleton instance of DBManager.
     * Uses double-checked locking pattern to ensure thread-safe lazy initialization
     * of the singleton instance.
     *
     * @return the singleton instance of DBManager
     */
    public static DBManager getInstance() {
        if (instance == null) {
            synchronized (DBManager.class) {
                if (instance == null) {
                    instance = new DBManager();
                }
            }
        }
        return instance;
    }

    /**
     * Retrieves a user from the cache by their username.
     *
     * @param username the username of the user to retrieve
     * @return the User object associated with the given username, or null if no
     *         user is found
     */
    public User getUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        String userId = usernameToId.get(username);
        if (userId == null) {
            return null;
        }
        return usersCache.get(userId);
    }

    public User getUserById(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return usersCache.get(userId);
    }

    /**
     * Adds a new user to the system.
     *
     * @param user the {@link User} object to be added
     * @return {@code true} if the user was successfully added, {@code false} if a
     *         user with the same username already exists
     */
    public synchronized boolean addNewUser(User user) {
        if (usernameToId.containsKey(user.getUsername())) {
            return false; // User already exists
        }
        if (usersCache.containsKey(user.getUserId())) {
            return false; // User ID already exists
        }
        usersCache.put(user.getUserId(), user);
        usernameToId.put(user.getUsername(), user.getUserId());
        saveUsers();
        return true;
    }

    /**
     * Authenticates a user by verifying their username and password against cached
     * user data.
     *
     * @param username the username of the user attempting to log in
     * @param password the password of the user attempting to log in
     * @return true if the user exists in the cache, the password matches and is not
     *         logged in; false otherwise
     */
    public DBStatus loginUser(String username, String password) {
        User user = getUserByUsername(username);

        if (user == null) {
            return DBStatus.USER_NOT_FOUND;
        }

        if (!user.getPassword().equals(password)) {
            return DBStatus.WRONG_PASSWORD;
        }

        return loggedInUsers.add(user.getUserId())
                ? DBStatus.SUCCESS
                : DBStatus.USER_ALREADY_LOGGED_IN;
    }

    /**
     * Logs out a user by removing them from the logged-in users collection.
     *
     * @param userId the userId of the user to logout
     * @return true if the user was successfully removed from the logged-in users,
     *         false if the user was not found in the logged-in users collection
     */
    public synchronized boolean logoutUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        return loggedInUsers.remove(userId);
    }

    public synchronized DBStatus updateCredentials(String oldUsername, String oldPassword, String newUsername,
            String newPassword) {
        User user = getUserByUsername(oldUsername);

        if (user == null) {
            return DBStatus.USER_NOT_FOUND;
        }

        if (!user.getPassword().equals(oldPassword)) {
            return DBStatus.WRONG_PASSWORD;
        }

        if (!oldUsername.equals(newUsername) && usernameToId.containsKey(newUsername)) {
            return DBStatus.USERNAME_ALREADY_EXISTS;
        }

        user.setUsername(newUsername);
        user.setPassword(newPassword);

        usernameToId.remove(oldUsername);
        usernameToId.put(newUsername, user.getUserId());

        saveUsers();

        return DBStatus.SUCCESS;
    }

    /**
     * Loads users from a JSON file and caches them in memory.
     * <p>
     * This method reads user data from the file path specified in the
     * configuration,
     * deserializes the JSON content into an array of User objects using Gson,
     * and populates the users cache with each user keyed by their username.
     * <p>
     * If any exception occurs during file reading or JSON deserialization,
     * it is wrapped in a RuntimeException with a descriptive error message.
     * <p>
     * 
     * @throws RuntimeException if the users file cannot be read or the JSON cannot
     *                          be parsed
     */
    synchronized private void loadUsers() {
        File file = new File(config.getUsersPath());
        if (!file.exists()) {
            return; // No users to load, start with an empty cache
        }

        try (FileReader reader = new FileReader(config.getUsersPath())) {
            Gson gson = new Gson();
            User[] usersArray = gson.fromJson(reader, User[].class);
            if (usersArray == null) {
                return;
            }
            for (User user : usersArray) {
                usersCache.put(user.getUserId(), user);
                usernameToId.put(user.getUsername(), user.getUserId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load users from database", e);
        }
    }

    /**
     * Saves all cached users to a JSON file.
     * 
     * <p>
     * This method serializes the users stored in {@code usersCache} to JSON format
     * and writes them to the file specified by {@code config.getUsersPath()}.
     * The file is automatically closed after writing.
     * 
     * @throws RuntimeException if an I/O error occurs while writing to the file
     * 
     */
    synchronized private void saveUsers() {
        try (FileWriter writer = new FileWriter(config.getUsersPath())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(usersCache.values(), writer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save users from database", e);
        }
    }

    /**
     * Loads the next game from the games JSON file using a persistent streaming
     * reader.
     * <p>
     * If the reader has not been opened yet or has reached the end of the array,
     * the games file is reopened from the beginning.
     *
     * @return the next available game, or null if the file contains no games
     */
    synchronized public Game loadNextGame() {
        try {
            if (gamesJsonReader == null || !gamesJsonReader.hasNext()) {
                openGamesReader();
            }

            return gamesJsonReader.hasNext()
                    ? gson.fromJson(gamesJsonReader, Game.class)
                    : null;
        } catch (Exception e) {
            closeGamesReader();
            throw new RuntimeException("Failed to load games from database", e);
        }
    }

    /**
     * Opens the games file reader from the beginning of the JSON array.
     * <p>
     * If a reader is already open, it is closed before creating the new one.
     *
     * @throws Exception if the games file cannot be opened or parsed
     */

    private void openGamesReader() throws Exception {
        closeGamesReader();
        gamesJsonReader = new JsonReader(new FileReader(config.getGamesPath()));
        gamesJsonReader.beginArray();
    }

    /**
     * Closes the current games reader and clears the associated reference.
     */
    private void closeGamesReader() {
        try {
            if (gamesJsonReader != null) {
                gamesJsonReader.close();
            }
        } catch (Exception e) {
            // Ignore cleanup errors, the reader is being recreated anyway.
        } finally {
            gamesJsonReader = null;
        }
    }

    /**
     * Saves the completed game state to the game history file in JSON format.
     * The method appends the serialized game state to the file, ensuring each entry
     * is separated by a newline.
     * This operation is synchronized to prevent concurrent access issues.
     *
     * @param gameState The completed game state to be saved.
     * @throws RuntimeException If an error occurs while writing to the game history
     *                          file.
     */
    synchronized public void saveGameHistory(CompletedGame gameState) {
        String historyPath = config.getGameHistoryPath();

        Map<String, CompletedGame> gameStateMap = new HashMap<>();

        File historyFile = new File(historyPath);
        try {
            if (historyFile.createNewFile()) {
                System.out.println("File created: " + historyFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.err.println("Failed to create stats file: " + e.getMessage());
            e.printStackTrace();
        }

        try (FileReader reader = new FileReader(historyPath)) {
            Type type = new TypeToken<Map<String, CompletedGame>>() {
            }.getType();
            gameStateMap = gson.fromJson(reader, type);
            if (gameStateMap == null) {
                gameStateMap = new HashMap<>();
            }
        } catch (Exception e) {
            System.err.println(
                    "Failed to read existing game history, starting with an empty history. Error: " + e.getMessage());
            gameStateMap = new HashMap<>();
        }

        gameStateMap.put(String.valueOf(gameState.getGameId()), gameState);

        try (FileWriter writer = new FileWriter(historyPath)) { // Append mode
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(gameStateMap, writer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save game history to database", e);
        }
    }

    /**
     * Reads the persisted game history file and returns the completed game with
     * the specified id.
     * <p>
     * The method deserializes a map of gameId &rarr; {@link CompletedGame} from
     * the configured history JSON file and looks up the requested id.
     * </p>
     *
     * @param gameId the id of the completed game to retrieve
     * @return the {@link CompletedGame} instance if present; {@code null} if the
     *         history file is empty or the id is not found
     * @throws RuntimeException if an error occurs while reading or parsing the
     *                          history file
     */
    synchronized public CompletedGame getCompletedGameById(int gameId) {
        String historyPath = config.getGameHistoryPath();

        try (FileReader reader = new FileReader(historyPath)) {
            Type type = new TypeToken<Map<String, CompletedGame>>() {
            }.getType();
            Map<String, CompletedGame> gameStateMap = gson.fromJson(reader, type);
            if (gameStateMap == null) {
                return null;
            }
            return gameStateMap.get(String.valueOf(gameId));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read game history from database", e);
        }
    }

    /**
     * Updates user statistics based on the provided game states.
     * <p>
     * Reads the current statistics from the JSON file, updates or creates
     * statistics for each user
     * based on their PlayerGameState, and saves the result to the file.
     * </p>
     * <ul>
     * <li>If the user has no statistics, creates a new entry.</li>
     * <li>Increments completed puzzles.</li>
     * <li>Records the number of mistakes (or "not finished" if the game was not
     * completed).</li>
     * <li>Handles streaks, perfect puzzles, and resets in case of defeat.</li>
     * </ul>
     * 
     * @param playerStatesByUserId map userId → PlayerGameState with the game states
     *                             to update
     * @throws RuntimeException if a read/write error occurs on the statistics file
     */
    synchronized public void updateUsersStats(Map<String, PlayerGameState> playerStatesByUserId) {

        String statsPath = config.getUsersStatsPath();

        Type type = new TypeToken<Map<String, UserStats>>() {
        }.getType();
        Map<String, UserStats> currPlayersStats;

        File statsFile = new File(statsPath);
        try {
            if (statsFile.createNewFile()) {
                System.out.println("File created: " + statsFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.err.println("Failed to create stats file: " + e.getMessage());
            e.printStackTrace();
        }

        try (FileReader reader = new FileReader(statsPath)) {

            Gson gson = new Gson();
            currPlayersStats = gson.fromJson(reader, type);
            if (currPlayersStats == null) {
                currPlayersStats = new HashMap<>();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read users stats from database", e);
        }

        // Update stats with new values
        for (Map.Entry<String, PlayerGameState> entry : playerStatesByUserId.entrySet()) {
            String userId = entry.getKey();
            PlayerGameState newStats = entry.getValue();

            // If the user has no existing stats, create a new entry.
            if (!currPlayersStats.containsKey(userId)) {
                int perfectGame = newStats.getMistakes() == 0 && newStats.isWinner() ? 1 : 0;
                MistakeHistogram mistakeHistogram = new MistakeHistogram();
                mistakeHistogram.increment(newStats.getMistakes());
                currPlayersStats.put(userId, new UserStats(1, 1, 1, perfectGame, mistakeHistogram));
                continue;
            }

            // Update existing stats by incrementing the relevant fields.
            UserStats existingStats = currPlayersStats.get(userId);
            existingStats.incrementPuzzlesCompleted();

            // If the game was not completed, we record it in the "not finished" bucket (5).
            if (!newStats.isCompleted()) {
                existingStats.recordMistake(5);
            } else { // Otherwise, we record the actual number of mistakes (0–4).
                existingStats.recordMistake(newStats.getMistakes());
            }

            // If the game was won, we increment the current streak and perfect puzzles if
            // there were no mistakes.
            if (newStats.isWinner()) {
                existingStats.incrementCurrentStreak();
                if (newStats.getMistakes() == 0) {
                    existingStats.incrementPerfectPuzzles();
                }
            }

            if (newStats.isLoser() || !newStats.isCompleted()) {
                existingStats.resetCurrentStreak();
            }
        }

        try (FileWriter writer = new FileWriter(statsPath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(currPlayersStats, writer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update users stats in database", e);
        }
    }

    /**
     * Retrieves statistics for the specified user from the statistics file.
     *
     * <p>
     * The method opens the JSON file located at {@code config.getUsersStatsPath()},
     * deserializes it into a {@code Map<String, UserStats>} and returns the
     * {@link UserStats} associated with {@code userId} if present.
     * </p>
     *
     * <p>
     * This method is {@code synchronized} because it performs I/O on a shared
     * statistics file and must be executed in a thread-safe manner relative to
     * other read/write operations.
     * </p>
     *
     * @param userId the identifier of the user whose statistics are requested
     * @return the {@link UserStats} instance for {@code userId}, or {@code null}
     *         if the file is empty or the user is not found
     * @throws RuntimeException if an error occurs while reading or parsing the
     *                          statistics file
     */
    synchronized public UserStats getUserStats(String userId) {
        String statsPath = config.getUsersStatsPath();

        try (FileReader reader = new FileReader(statsPath)) {
            Type type = new TypeToken<Map<String, UserStats>>() {
            }.getType();
            Map<String, UserStats> gameStateMap = gson.fromJson(reader, type);
            if (gameStateMap == null) {
                return null;
            }
            return gameStateMap.get(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read user stats from database", e);
        }
    }

}
