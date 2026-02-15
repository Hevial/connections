package client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.google.gson.Gson;

import client.handlers.ResponseHandler;
import client.menus.BaseMenu;
import client.menus.MainMenu;
import models.Request;
import models.Response;

public class ClientMain {

    private static Scanner scanner = new Scanner(System.in);
    private static BaseMenu currentMenu;
    private static ResponseHandler responseHandler = new ResponseHandler(scanner);

    public static void main(String[] args) {
        try {
            currentMenu = new MainMenu(scanner);
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

            ByteBuffer buffer = ByteBuffer.allocate(4096);
            Gson gson = new Gson();
            while (true) {

                currentMenu.show();
                int choice = currentMenu.getChoice();

                // Build request based on user choice
                Request req = currentMenu.handleChoice(choice);
                String reqStr = gson.toJson(req, Request.class);

                // Send request to server
                buffer.clear(); // Clear buffer before writing
                buffer.put(reqStr.getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                socket.write(buffer);

                // Read response from server
                buffer.clear();
                int bytesRead = socket.read(buffer);
                if (bytesRead == -1) {
                    System.out.println("Connessione chiusa dal server.");
                    break;
                }
                buffer.flip();

                // Convert response bytes to string
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                String responseStr = new String(bytes, StandardCharsets.UTF_8);

                // Debug: print the raw response string from the server
                System.out.println("Risposta dal server: " + responseStr);

                // Parse response
                Response res = gson.fromJson(responseStr, Response.class);

                currentMenu = responseHandler.handleResponse(res, currentMenu);
            }
        } catch (java.net.ConnectException ce) {
            System.out.println("Impossibile connettersi al server: " + ce.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}