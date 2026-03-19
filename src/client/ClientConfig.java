package client;

import java.io.FileReader;
import com.google.gson.Gson;

/**
 * Configuration holder for client connection settings.
 *
 * <p>
 * This immutable value object contains the server hostname and port
 * used by the client to connect to the server. Instances are typically
 * created by {@link #loadConfig()} which reads the JSON configuration
 * file {@code src/resources/configs/ClientConfig.json} using Gson.
 * </p>
 */
public class ClientConfig {

    private int serverPort;
    private String serverHostname;

    /**
     * Create a new ClientConfig.
     *
     * @param serverPort     the port number of the server
     * @param serverHostname the hostname or IP address of the server
     */
    public ClientConfig(int serverPort, String serverHostname) {
        this.serverPort = serverPort;
        this.serverHostname = serverHostname;
    }

    /**
     * Return the configured server port.
     *
     * @return server port number
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Return the configured server hostname or IP address.
     *
     * @return server hostname
     */
    public String getServerHostname() {
        return serverHostname;
    }

    /**
     * Load the client configuration from the JSON file at
     * {@code src/resources/configs/ClientConfig.json}.
     *
     * <p>
     * This method uses Gson to parse the file into a {@link ClientConfig}
     * instance. It propagates exceptions (I/O or JSON parsing) to the caller
     * so the application can decide how to handle configuration errors.
     * </p>
     *
     * @return a populated {@link ClientConfig}
     * @throws Exception on I/O or parsing errors
     */
    public static ClientConfig loadConfig() throws Exception {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("src/resources/configs/ClientConfig.json")) {
            return gson.fromJson(reader, ClientConfig.class);
        }
    }
}
