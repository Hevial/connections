package server;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    public static void main(String[] args) {
        try {
            ServerConfig config = ServerConfig.loadConfig();
            startServer(config);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void startServer(ServerConfig config) {
        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            System.out.println("Server started on port " + serverSocket.getLocalPort());
            System.out.println("Host: " + config.getHost());
            System.out.println("Game Duration: " + config.getGameDuration() + "s");

            while (true) {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
