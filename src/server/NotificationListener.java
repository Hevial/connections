package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.db.DBManager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple UDP listener to accept client pokes and keep a UDP port bound on the
 * server side. This helps NAT mappings on many networks so the server can send
 * UDP notifications back to clients.
 *
 * Expected poke payload (JSON): {"userId":"<user-id>"}
 * The listener will prefer the observed {@link DatagramPacket#getAddress()} and
 * {@link DatagramPacket#getPort()} as the source address and register it in
 * {@link NotificationRegistry} associated with the provided userId.
 */
public class NotificationListener implements Runnable {

    private static final Logger LOG = Logger.getLogger(NotificationListener.class.getName());
    private static final Gson GSON = new Gson();

    private final int port;
    private volatile boolean running = true;

    /**
     * Create a notification listener that binds to the specified UDP port.
     *
     * @param port UDP port on which to listen for client pokes
     */
    public NotificationListener(int port) {
        this.port = port;
    }

    /**
     * Request an orderly shutdown of the listener loop. The {@link #run()}
     * method will observe the flag and exit shortly after the next receive
     * attempt returns.
     */
    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            while (running) {
                try {
                    socket.receive(packet);
                    String s = new String(packet.getData(), packet.getOffset(), packet.getLength(),
                            StandardCharsets.UTF_8);
                    LOG.info("Received UDP poke from " + packet.getAddress() + ":" + packet.getPort()
                            + " payload=" + s);

                    // Try to parse username from JSON payload and register the observed address
                    try {
                        JsonObject obj = GSON.fromJson(s, JsonObject.class);
                        if (obj != null) {
                            String username = null;
                            if (obj.has("username") && !obj.get("username").isJsonNull()) {
                                username = obj.get("username").getAsString();
                            }

                            if (username != null && !username.isEmpty()) {
                                // validate that username corresponds to a logged-in user
                                DBManager db = DBManager.getInstance();
                                if (db.isUserLoggedInByUsername(username)) {
                                    String userId = db.getUserByUsername(username).getUserId();
                                    InetSocketAddress addr = new InetSocketAddress(packet.getAddress(),
                                            packet.getPort());
                                    NotificationRegistry.register(userId, addr);
                                    LOG.fine("NotificationListener: registered " + username + " -> " + addr);
                                } else {
                                    LOG.fine("NotificationListener: received poke for not-logged-in username: "
                                            + username);
                                }
                            } else {
                                LOG.fine("NotificationListener: poke without username received; ignoring registration");
                            }
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Failed to parse poke payload or register address", e);
                    }
                } catch (Exception e) {
                    if (running) {
                        LOG.log(Level.WARNING, "Error while receiving UDP poke", e);
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "NotificationListener failed to bind UDP port " + port, e);
        }
    }

}
