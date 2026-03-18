package server;

import java.io.FileReader;
import com.google.gson.Gson;

/**
 * Configuration holder for server settings loaded from
 * {@code src/resources/configs/ServerConfig.json}.
 */
public class ServerConfig {
    private int port;
    private String hostname;
    private int gameDuration;

    public ServerConfig(int port, String hostname, int gameDuration) {
        this.port = port;
        this.hostname = hostname;
        this.gameDuration = gameDuration;
    }

    /** @return TCP port the server should bind to */
    public int getPort() {
        return port;
    }

    /** @return configured host name or bind address */
    public String getHost() {
        return hostname;
    }

    /** @return default game duration in seconds */
    public int getGameDuration() {
        return gameDuration;
    }

    /**
     * Load server configuration from the JSON file.
     *
     * @return populated {@link ServerConfig}
     * @throws Exception if the configuration file cannot be read or parsed
     */
    public static ServerConfig loadConfig() throws Exception {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("src/resources/configs/ServerConfig.json")) {
            return gson.fromJson(reader, ServerConfig.class);
        }
    }
}
