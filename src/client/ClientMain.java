package client;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

import client.handlers.ResponseHandler;
import client.menus.BaseMenu;
import client.menus.MainMenu;
import models.Request;
import models.Response;

public class ClientMain {

    private static BaseMenu currentMenu;
    private static ResponseHandler responseHandler = new ResponseHandler();

    // UDP notification handling
    private static int notifyPort = -1;
    private static volatile boolean notificationPending = false;
    private static volatile String notificationMessage = null;
    private static NotificationClient notificationClient = null;

    public static int getNotifyPort() {
        return notifyPort;
    }

    public static boolean hasPendingNotification() {
        return notificationPending;
    }

    public static void pushNotification(String msg) {
        // Mark notification as pending and print a concise inline message.
        // We avoid calling menu redraws from this async thread to prevent
        // clearing the user's input. Instead we print the notification and
        // reprint the simple prompt so the user sees it immediately.
        notificationMessage = msg;
        notificationPending = true;
        try {
            synchronized (System.out) {
                System.out.println();
                System.out.println("╠ NOTIFICA: " + msg);
                // clear any queued lines the user may have submitted
                try {
                    InputReader.clearQueue();
                } catch (Throwable ignored) {
                }
                System.out.print("╠ Seleziona un'opzione: ");
                System.out.flush();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Central handler to register an incoming notification and force the UI
     * back to the main menu. Safe to call from menu code when consuming a
     * pending notification.
     */
    public static void handleIncomingNotification(String msg) {
        // Only record the pending notification; do NOT switch menus here.
        notificationMessage = msg;
        notificationPending = true;
    }

    /**
     * Consume pending notification if any and return its message, or null.
     */
    public static String consumeNotification() {
        if (notificationPending) {
            notificationPending = false;
            String m = notificationMessage;
            notificationMessage = null;
            return m;
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            currentMenu = new MainMenu();
            ClientConfig config = ClientConfig.loadConfig();
            startClient(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a single UDP poke (JSON with username) to the server using the
     * existing UDP socket. This should be invoked after a successful login.
     *
     * @param username the logged-in username to include in the poke payload
     */
    public static void sendLoginPoke(String username) {
        if (notificationClient == null || username == null)
            return;
        notificationClient.sendPoke(username);
    }

    /**
     * Start periodic keepalive pokes (default interval 30s).
     */
    public static void startNotificationKeepalive(String username) {
        if (notificationClient == null || username == null)
            return;
        notificationClient.startKeepalive(username, 30);
    }

    /** Stop periodic keepalive pokes. */
    public static void stopNotificationKeepalive() {
        if (notificationClient == null)
            return;
        notificationClient.stopKeepalive();
    }

    private static void startClient(ClientConfig config) {
        System.out.println("Client will connect to " + config.getServerHostname() + ":" + config.getServerPort());

        InetSocketAddress serverAddress = new InetSocketAddress(config.getServerHostname(), config.getServerPort());

        // Start NotificationClient (listener + poke/keepalive helper)
        NotificationClient nc = null;
        try {
            nc = new NotificationClient(serverAddress);
            nc.start();
            notifyPort = nc.getLocalPort();
            notificationClient = nc;
        } catch (Exception e) {
            System.out.println("Impossibile avviare listener UDP per notifiche: " + e.getMessage());
            if (nc != null)
                try {
                    nc.close();
                } catch (Exception ignored) {
                }
        }

        // Start non-blocking input reader so menus can be interrupted by notifications
        InputReader.start();

        try (SocketChannel socket = SocketChannel.open(serverAddress)) {
            System.out.println("Connected to server: " + socket.getRemoteAddress());

            ByteBuffer buffer = ByteBuffer.allocate(4096);
            Gson gson = new Gson();
            while (true) {

                currentMenu.show();
                int choice = currentMenu.getChoice();
                if (choice == -1) {
                    // Interrupted by notification -> restart loop to redraw menu with notification
                    // message
                    continue;
                }

                Request req = currentMenu.handleChoice(choice);
                if (req == null) {
                    continue;
                }

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
        } finally {
            if (notificationClient != null) {
                try {
                    notificationClient.close();
                } catch (Exception ignored) {
                }
            }
            try {
                InputReader.stop();
            } catch (Exception ignored) {
            }
        }
    }

}
