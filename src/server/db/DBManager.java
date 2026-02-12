package server.db;

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
    private ConcurrentHashMap<String, User> usersCache;
    private Set<String> loggedInUsers;

    private DBManager() {
        try {
            usersCache = new ConcurrentHashMap<>();
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
        return usersCache.get(username);
    }

    /**
     * Adds a new user to the system.
     *
     * @param user the {@link User} object to be added
     * @return {@code true} if the user was successfully added, {@code false} if a
     *         user with the same username already exists
     */
    public boolean addNewUser(User user) {
        if (usersCache.putIfAbsent(user.getUsername(), user) != null) {
            return false; // User already exists
        }
        saveUsers();
        return true;
    }

    /**
     * Authenticates a user by verifying their username and password against cached
     * user data.
     *
     * @param username the username of the user attempting to log in
     * @param password the password of the user attempting to log in
     * @return true if the user exists in the cache and the password matches; false
     *         otherwise
     */
    public boolean loginUser(String username, String password) {
        User user = usersCache.get(username);
        if (user != null && user.getPassword().equals(password)) { // User exists and password matches, log them in
            loggedInUsers.add(username);
            return true;
        }
        return false;
    }

    /**
     * Logs out a user by removing them from the logged-in users collection.
     *
     * @param username the username of the user to logout
     * @return true if the user was successfully removed from the logged-in users,
     *         false if the user was not found in the logged-in users collection
     */
    public boolean logoutUser(String username) {
        return loggedInUsers.remove(username);
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
    private void loadUsers() {
        try (FileReader reader = new FileReader(config.getUsersPath())) {
            Gson gson = new Gson();
            User[] usersArray = gson.fromJson(reader, User[].class);
            for (User user : usersArray) {
                usersCache.put(user.getUsername(), user);
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
    private void saveUsers() {
        try (FileWriter writer = new FileWriter(config.getUsersPath())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(usersCache.values(), writer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load users from database", e);
        }
    }

}
