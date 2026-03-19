package client;

import java.net.ConnectException;
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

/**
 * Main client application entry and helper utilities.
 *
 * <p>
 * This class drives the console client lifecycle: it initializes the
 * user interface menus, manages the notification listener, and handles the
 * main request/response loop communicating with the server over TCP.
 */
public class ClientMain {

    private static BaseMenu currentMenu;
    private static ResponseHandler responseHandler = new ResponseHandler();

    // UDP notification handling
    /**
     * Local UDP port used by the {@link NotificationClient} listener. Set to
     * {@code -1} when the listener is not running.
     */
    private static int notifyPort = -1;

    /**
     * Flag indicating whether a notification message is pending consumption by
     * the interactive UI.
     */
    private static volatile boolean notificationPending = false;

    /**
     * The currently pending notification message (consumed by
     * {@link #consumeNotification()}). May be {@code null} when no message is
     * pending.
     */
    private static volatile String notificationMessage = null;

    /**
     * Helper that manages UDP notification listening and poke/keepalive
     * operations. Null when notifications are not configured or the listener
     * failed to start.
     */
    private static NotificationClient notificationClient = null;

    /**
     * Returns the local UDP port used for receiving notifications, or -1 if
     * the notification listener is not running.
     *
     * @return local notification UDP port or -1
     */
    public static int getNotifyPort() {
        return notifyPort;
    }

    /**
     * Returns whether a notification is pending consumption by the UI.
     *
     * @return true if a notification message is pending, false otherwise
     */
    public static boolean hasPendingNotification() {
        return notificationPending;
    }

    /**
     * Called by the asynchronous notification listener to deliver a new
     * notification message to the interactive UI.
     *
     * <p>
     * The method marks the notification as pending and prints a concise
     * inline message to the console. It avoids redrawing complex UI elements
     * from the notification thread.
     *
     * @param msg      the notification message to display (may be null)
     * @param gameData optional game data attached to the notification; if
     *                 present it will be passed to the current menu
     */
    public static void pushNotification(String msg, String gameData) {
        // Mark notification as pending and print a concise inline message.
        // We avoid calling menu redraws from this async thread to prevent
        // clearing the user's input. Instead we print the notification and
        // reprint the simple prompt so the user sees it immediately.
        notificationMessage = msg;
        notificationPending = true;

        if (gameData != null) {
            currentMenu.setGameData(gameData);
        }

        try {
            synchronized (System.out) {
                System.out.println();
                System.out.println(msg);
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
     * Consume and return the currently pending notification message.
     *
     * @return the pending notification message, or null if none was pending
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

    /**
     * Application entry point.
     *
     * @param args command-line arguments (ignored)
     */
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
     * Send a single UDP poke to the server to announce the logged-in user.
     *
     * <p>
     * This helper forwards the username to the {@link NotificationClient}
     * which transmits a small JSON payload; used to keep server-side presence
     * information up-to-date.
     *
     * @param username the logged-in username to include in the poke payload
     */
    public static void sendLoginPoke(String username) {
        if (notificationClient == null || username == null)
            return;
        notificationClient.sendPoke(username);
    }

    /**
     * Start periodic keepalive pokes for the given username.
     *
     * @param username the username to keep alive; if null the call is ignored
     */
    public static void startNotificationKeepalive(String username) {
        if (notificationClient == null || username == null)
            return;
        notificationClient.startKeepalive(username, 30);
    }

    /**
     * Stop the periodic notification keepalive task if running.
     */
    public static void stopNotificationKeepalive() {
        if (notificationClient == null)
            return;
        notificationClient.stopKeepalive();
    }

    /**
     * Initialize client subsystems and run the main request/response loop.
     *
     * <p>
     * This method performs the following steps:
     * <ol>
     * <li>Starts the {@link NotificationClient} (if available) and records
     * its local UDP port.</li>
     * <li>Starts the non-blocking {@link InputReader} so the UI can be
     * interrupted by notifications.</li>
     * <li>Opens a TCP {@link SocketChannel} to the server and runs a loop
     * that renders the current menu, collects the user's choice, sends
     * the serialized {@link models.Request}, receives the server
     * {@link models.Response} and delegates handling to
     * {@link ResponseHandler}.</li>
     * </ol>
     * </p>
     *
     * <p>
     * IO and network exceptions are propagated to the caller of
     * {@link #main(String[])}, which prints the stack trace. This method is
     * intentionally kept package-private to be invoked only from this class.
     * </p>
     *
     * @param config the client configuration with server hostname and port
     */
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
                // System.out.println("Risposta dal server: " + responseStr);

                // Parse response
                Response res = gson.fromJson(responseStr, Response.class);

                currentMenu = responseHandler.handleResponse(res, currentMenu);
            }
        } catch (ConnectException ce) {
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
