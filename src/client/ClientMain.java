package client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

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

        SocketAddress serverAddress = new InetSocketAddress(config.getServerHostname(), config.getServerPort());
        try (SocketChannel socket = SocketChannel.open(serverAddress)) {
            System.out.println("Connected to server: " + socket.getRemoteAddress());

            Scanner scanner = new Scanner(System.in);
            System.out.print("Benvenuto!");

            String message;
            do {
                System.out.print("\nTU >> ");
                message = scanner.nextLine();
            } while (message != null && !message.equalsIgnoreCase("exit"));

            scanner.close();
        } catch (java.net.ConnectException ce) {
            System.out.println("Impossibile connettersi al server: " + ce.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
