package client;

import java.io.FileReader;
import com.google.gson.Gson;

public class ClientConfig {

    private int serverPort;
    private String serverHostname;

    public ClientConfig(int serverPort, String serverHostname) {
        this.serverPort = serverPort;
        this.serverHostname = serverHostname;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerHostname() {
        return serverHostname;
    }

    public static ClientConfig loadConfig() throws Exception {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("src/resources/configs/ClientConfig.json")) {
            return gson.fromJson(reader, ClientConfig.class);
        }
    }
}
