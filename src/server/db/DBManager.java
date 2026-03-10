package server.db;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import models.User;

public class DBManager {

    private static DBManager instance;
    private DBConfig config;
    private ConcurrentHashMap<String, User> usersCache; // key: userId, value: User
    private ConcurrentHashMap<String, String> usernameToId; // key: username, value: userId
    private Set<String> loggedInUsers;

    private DBManager() {
        try {
            usersCache = new ConcurrentHashMap<>();
            usernameToId = new ConcurrentHashMap<>();
            loggedInUsers = ConcurrentHashMap.newKeySet();
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

}
