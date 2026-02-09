package client;

import java.net.Socket;

public class ClientMain {

    public static void main(String[] args) {
        try {
            // Load configuration
            ClientConfig config = ClientConfig.loadConfig();
            startClient(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startClient(ClientConfig config) {
        System.out.println("Client will connect to " + config.getServerHostname() + ":" + config.getServerPort());

        try (Socket socket = new Socket(config.getServerHostname(), config.getServerPort())) {
            System.out.println("Connected to server: " + socket.getInetAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
