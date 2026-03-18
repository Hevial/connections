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
 * Client-side notification helper: manages a UDP socket for receiving
 * notifications, sending registration pokes and an optional keepalive.
 */
public class NotificationClient implements AutoCloseable, Runnable {

    private static final Logger LOG = Logger.getLogger(NotificationClient.class.getName());
    private static final Gson GSON = new Gson();

    private final InetSocketAddress serverAddress;
    private DatagramSocket socket;
    private final ExecutorService receiver = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "client-udp-receiver");
        t.setDaemon(true);
        return t;
    });
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "client-udp-keepalive");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledFuture<?> keepaliveTask;

    public NotificationClient(InetSocketAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Binds an ephemeral UDP port and starts the receive loop.
     */
    public void start() throws Exception {
        if (running.get())
            return;
        socket = new DatagramSocket(0);
        running.set(true);
        receiver.submit(this);
        LOG.fine("NotificationClient started on port " + socket.getLocalPort());
    }

    public int getLocalPort() {
        return socket == null ? -1 : socket.getLocalPort();
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    /**
     * Send a single poke. the payload will be {"username": username}
     * The server is expected to respond to this poke by registering the observed
     * UDP source address and associating it with the provided username, so that
     * notifications can be sent back to that address. This should be invoked after
     * a successful login
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
     * Start periodic keepalive pokes. The same value/token will be sent every
     * intervalSecs.
     */
    public void startKeepalive(String usename, long intervalSecs) {
        stopKeepalive();
        if (intervalSecs <= 0)
            return;
        keepaliveTask = scheduler.scheduleAtFixedRate(() -> sendPoke(usename), intervalSecs, intervalSecs,
                TimeUnit.SECONDS);
        LOG.fine("Keepalive scheduled every " + intervalSecs + "s");
    }

    public void stopKeepalive() {
        if (keepaliveTask != null && !keepaliveTask.isCancelled())
            keepaliveTask.cancel(true);
        keepaliveTask = null;
    }

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
