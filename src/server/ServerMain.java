package server;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import server.handlers.RequestHandler;

/**
 * Server application entry point and TCP accept loop.
 *
 * <p>
 * This class loads the server configuration, starts the periodic
 * {@link GameManager} scheduler, spins up the UDP notification listener and
 * accepts incoming TCP client connections, handing each connection to a
 * {@link ServerWorker} executed by a thread pool.
 * </p>
 */
public class ServerMain {

    /**
     * Process command-line arguments and start the server.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        try {
            ServerConfig config = ServerConfig.loadConfig();
            startServer(config);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Initialize server resources and run the accept loop.
     *
     * @param config server configuration loaded from JSON
     */
    private static void startServer(ServerConfig config) {
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            serverSocket.bind(new InetSocketAddress(config.getHost(), config.getPort()));
            System.out.println("Server configuration loaded successfully:");
            System.out.println("Server started on port " + serverSocket.getLocalAddress());
            System.out.println("Host: " + config.getHost());
            System.out.println("Game Duration: " + config.getGameDuration() + "s\n\n");

            ExecutorService executor = Executors.newCachedThreadPool();
            ScheduledExecutorService gameExecutorService = Executors.newSingleThreadScheduledExecutor();
            GameManager gameManager = new GameManager(config.getGameDuration());
            gameExecutorService.scheduleWithFixedDelay(gameManager, 0,
                    config.getGameDuration(), TimeUnit.SECONDS);

            // Start UDP listener to accept client pokes on the configured port
            ExecutorService udpExecutor = Executors.newSingleThreadExecutor();
            udpExecutor.execute(new NotificationListener(config.getPort()));

            while (true) {
                // Wait for a client to connect
                SocketChannel clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getRemoteAddress());

                // Handle the client connection in a separate thread
                executor.execute(new ServerWorker(new RequestHandler(gameManager), clientSocket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
