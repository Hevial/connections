package server;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            serverSocket.bind(new InetSocketAddress(config.getHost(), config.getPort()));
            System.out.println("Server started on port " + serverSocket.getLocalAddress());
            System.out.println("Host: " + config.getHost());
            System.out.println("Game Duration: " + config.getGameDuration() + "s");

            ExecutorService executor = Executors.newCachedThreadPool();

            while (true) {
                // Wait for a client to connect
                SocketChannel clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getRemoteAddress());

                // Handle the client connection in a separate thread
                executor.submit(new ServerWorker(new RequestHandler(), clientSocket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
