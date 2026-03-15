package server.db;

import java.io.FileReader;

import com.google.gson.Gson;

/**
 * Configuration class for database paths loaded from a JSON configuration file.
 * <p>
 * This class manages the file paths all the data storage. It
 * provides access to these paths through getter methods and supports loading
 * configuration from an external JSON file using Gson deserialization.
 * </p>
 */
public class DBConfig {

    private String gamesPath;
    private String usersPath;
    private String gameHistoryPath;
    private String usersStatsPath;

    public String getGamesPath() {
        return gamesPath;
    }

    public String getUsersPath() {
        return usersPath;
    }

    public String getGameHistoryPath() {
        return gameHistoryPath;
    }

    public String getUsersStatsPath() {
        return usersStatsPath;
    }

    /**
     * Loads the database configuration from a JSON file.
     * 
     * Reads the configuration file located at "src/resources/configs/DBConfig.json"
     * and deserializes it into a DBConfig object using Gson.
     * 
     * @return a new DBConfig instance populated with values from the JSON file
     * @throws Exception if an I/O error occurs while reading the file or if JSON
     *                   deserialization fails
     */
    public static DBConfig loadConfig() throws Exception {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("src/resources/configs/DBConfig.json")) {
            return gson.fromJson(reader, DBConfig.class);
        }
    }
}
