package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends UDP notifications to registered clients.
 */
public class NotificationSender {

    private static final Logger LOG = Logger.getLogger(NotificationSender.class.getName());
    private static final Gson GSON = new Gson();

    // TODO add playerGameStatus in the notification payload and next game status to
    // update the client
    public static void notifyGameEnd(models.CompletedGame completedGame) {
        JsonObject envelope = new JsonObject();
        envelope.add("GameEndNotification", GSON.toJsonTree(completedGame));
        String payload = envelope.toString();
        LOG.info("notifyGameEnd: sending GameEndNotification payload size=" + payload.length());
        sendToAll(payload);
    }

    /**
     * Sends the given JSON payload via UDP to all addresses returned by
     * {@link NotificationRegistry#getAllAddresses()}.
     *
     * The payload is encoded as UTF-8 and sent as a single
     * {@link java.net.DatagramPacket} to each recipient address. Each send
     * attempt is performed independently: failures for a specific address are
     * logged and do not prevent attempts to send to the remaining addresses.
     * A {@link java.net.DatagramSocket} is created using try-with-resources,
     * so it is automatically closed when the operation completes.
     *
     * Note: UDP is a best-effort transport and does not guarantee delivery or
     * ordering of packets. Use this method only for non-critical
     * notifications where occasional loss is acceptable.
     *
     * @param payload the JSON string to send; must not be {@code null}
     */
    private static void sendToAll(String payload) {
        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
        try (DatagramSocket socket = new DatagramSocket()) {
            for (InetSocketAddress addr : NotificationRegistry.getAllAddresses()) {
                LOG.info("NotificationSender: sending to " + addr);
                try {
                    DatagramPacket packet = new DatagramPacket(data, data.length, addr.getAddress(), addr.getPort());
                    socket.send(packet);
                    LOG.info("NotificationSender: sent to " + addr + " bytes=" + data.length);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to send notification to " + addr, e);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create UDP socket for notifications", e);
        }
    }

}
