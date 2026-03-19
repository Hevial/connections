package client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import models.CompletedGame;
import models.PlayerCompletedGame;
import models.PlayerGameState;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client-side notification helper that manages UDP-based notifications.
 *
 * <p>
 * This class provides three primary responsibilities:
 * <ol>
 * <li>Bind an ephemeral UDP port and receive server-sent notifications.</li>
 * <li>Send a registration "poke" to inform the server of the client's
 * observed UDP endpoint and associated username (used after login).</li>
 * <li>Optionally schedule periodic keepalive pokes to keep NAT mappings
 * alive on the server side.</li>
 * </ol>
 *
 * <p>
 * Notifications are received on a dedicated receiver thread and processed
 * by {@link #run()}; formatted notification text is forwarded to the
 * interactive UI via {@link ClientMain#pushNotification(String, String)}.
 * The class implements {@link AutoCloseable} so resources (socket and
 * executors) can be released with {@link #close()}.
 * </p>
 *
 * @see ClientMain#pushNotification(String, String)
 */
public class NotificationClient implements AutoCloseable, Runnable {

    private static final Logger LOG = Logger.getLogger(NotificationClient.class.getName());
    private static final Gson GSON = new Gson();

    /** Remote server address used for pokes and keepalive packets. */
    private final InetSocketAddress serverAddress;

    /**
     * Datagram socket bound to an ephemeral local port. May be {@code null}
     * until {@link #start()} is called.
     */
    private DatagramSocket socket;

    /** Single-thread executor used to run the UDP receive loop. */
    private final ExecutorService receiver = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "client-udp-receiver");
        t.setDaemon(true);
        return t;
    });
    /** Scheduler for periodic keepalive pokes. */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "client-udp-keepalive");
        t.setDaemon(true);
        return t;
    });
    /** Flag indicating whether the receiver loop should continue running. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** Scheduled future for the keepalive task, if any. */
    private ScheduledFuture<?> keepaliveTask;

    /**
     * Create a NotificationClient bound to the provided server address.
     *
     * @param serverAddress the server UDP address used for pokes and notifications
     */
    public NotificationClient(InetSocketAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Bind an ephemeral UDP port and start the receive loop.
     *
     * @throws Exception if opening the socket fails
     */
    public void start() throws Exception {
        if (running.get())
            return;
        socket = new DatagramSocket(0);
        running.set(true);
        receiver.submit(this);
        LOG.fine("NotificationClient started on port " + socket.getLocalPort());
    }

    /**
     * Return the local UDP port number to which the client socket is bound.
     *
     * @return the local port number, or {@code -1} if the socket is not yet bound
     */
    public int getLocalPort() {
        return socket == null ? -1 : socket.getLocalPort();
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    /**
     * Send a single registration poke to the server.
     *
     * <p>
     * The payload is a small JSON object of the form
     * {@code {"username": "<username>"}}. The server is expected to record
     * the observed UDP source address and associate it with the provided
     * username so that later notifications can be delivered to this client.
     * This method is typically called after a successful login.
     * </p>
     *
     * @param username the username to register with the server; if {@code null}
     *                 the method returns without sending
     */
    public void sendPoke(String username) {
        if (socket == null || serverAddress == null || username == null)
            return;
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("username", username);
            byte[] data = obj.toString().getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress.getAddress(),
                    serverAddress.getPort());
            socket.send(packet);
            LOG.fine("Sent poke to " + serverAddress + " payload=" + obj.toString());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send poke", e);
        }
    }

    /**
     * Start periodic keepalive pokes for the given username.
     *
     * <p>
     * Scheduling a keepalive repeatedly sends the same JSON registration
     * payload to the server to refresh any NAT bindings and to keep the
     * server-side registration active.
     * </p>
     *
     * @param usename      the username to send in keepalive pokes (note parameter
     *                     name)
     * @param intervalSecs interval in seconds between pings; values &lt;= 0 are
     *                     ignored
     */
    public void startKeepalive(String usename, long intervalSecs) {
        stopKeepalive();
        if (intervalSecs <= 0)
            return;
        keepaliveTask = scheduler.scheduleAtFixedRate(() -> sendPoke(usename), intervalSecs, intervalSecs,
                TimeUnit.SECONDS);
        LOG.fine("Keepalive scheduled every " + intervalSecs + "s");
    }

    /**
     * Cancel any active keepalive task.
     *
     * <p>
     * If a periodic keepalive was scheduled with
     * {@link #startKeepalive(String, long)},
     * this method attempts to cancel it. The scheduler itself is not shut down
     * here; call {@link #close()} to release all resources.
     * </p>
     */
    public void stopKeepalive() {
        if (keepaliveTask != null && !keepaliveTask.isCancelled())
            keepaliveTask.cancel(true);
        keepaliveTask = null;
    }

    /**
     * Close the notification client and release resources.
     *
     * <p>
     * This stops the receive loop, cancels any scheduled keepalive, closes
     * the UDP socket and shuts down internal executors. This method is safe to
     * call multiple times.
     * </p>
     */
    @Override
    public void close() {
        running.set(false);
        stopKeepalive();
        try {
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (Exception ignored) {
        }
        try {
            receiver.shutdownNow();
        } catch (Exception ignored) {
        }
        try {
            scheduler.shutdownNow();
        } catch (Exception ignored) {
        }
    }

    /**
     * Receive loop that listens for UDP notifications from the server.
     *
     * <p>
     * This method runs on the dedicated receiver thread created by the
     * executor. It blocks on {@link DatagramSocket#receive(DatagramPacket)} and
     * attempts to parse each incoming packet as JSON. Recognized payload
     * elements (for example {@code CompletedGame}, {@code PlayerCompletedGame}
     * or {@code PlayerGameState}) are converted to their model types and
     * formatted for display. Formatted text and optional structured game data
     * are forwarded to the interactive UI by calling
     * {@link ClientMain#pushNotification(String, String)}.
     * </p>
     *
     * <p>
     * Exceptions encountered while receiving or parsing packets are logged
     * and will terminate the receive loop. Shutdown is cooperative: calling
     * {@link #close()} or setting {@link #running} to {@code false} will
     * cause the loop to exit. This method does not throw checked exceptions.
     * </p>
     *
     * <p>
     * Threading note: the method is intended to execute on a single
     * dedicated thread. Concurrent callers may safely invoke {@link #close()} to
     * request orderly shutdown.
     * </p>
     */
    @Override
    public void run() {
        byte[] buf = new byte[8192];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while (running.get() && socket != null && !socket.isClosed()) {
            try {
                socket.receive(packet);
                String s = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                JsonObject obj = GSON.fromJson(s, JsonObject.class);
                if (obj == null)
                    continue;

                // Build a notification payload string for the UI. The payload may
                // contain a completed-game summary, a player-specific completed
                // game view, and an optional player game state representation.
                //
                StringBuilder sb = new StringBuilder();
                sb.append("╠ NOTIFICA DI FINE PARTITA \n");

                JsonElement completedGameElem = obj.get("CompletedGame");
                if (completedGameElem != null && completedGameElem.isJsonObject()) {
                    CompletedGame g = GSON.fromJson(completedGameElem, CompletedGame.class);
                    sb.append(CompletedGameFormatter.formatSummary(g));

                }

                JsonElement playerCompletedGameElem = obj.get("PlayerCompletedGame");
                if (playerCompletedGameElem != null && playerCompletedGameElem.isJsonObject()) {
                    PlayerCompletedGame pg = GSON.fromJson(playerCompletedGameElem, PlayerCompletedGame.class);
                    sb.append(CompletedGameFormatter.formatForUser(pg));
                }

                String gameData = null;
                JsonElement playerGameStateElem = obj.get("PlayerGameState");
                if (playerGameStateElem != null && playerGameStateElem.isJsonObject()) {
                    PlayerGameState newGameState = GSON.fromJson(playerGameStateElem, PlayerGameState.class);
                    gameData = PlayerGameStateFormatter.format(newGameState);
                    sb.append("║\n").append("╠ Nuova partita iniziata! Preparati per la prossima sfida.\n");
                }

                ClientMain.pushNotification(sb.toString(), gameData);
            } catch (Exception e) {
                if (running.get())
                    LOG.log(Level.WARNING, "UDP receive error", e);
                break;
            }
        }
    }
}
