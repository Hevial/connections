package server;

import java.io.FileReader;
import com.google.gson.Gson;

public class ServerConfig {
    private int port;
    private String hostname;
    private int gameDuration;

    public ServerConfig(int port, String hostname, int gameDuration) {
        this.port = port;
        this.hostname = hostname;
        this.gameDuration = gameDuration;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return hostname;
    }

    public int getGameDuration() {
        return gameDuration;
    }

    public static ServerConfig loadConfig() throws Exception {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("src/resources/configs/ServerConfig.json")) {
            return gson.fromJson(reader, ServerConfig.class);
        }
    }
}
