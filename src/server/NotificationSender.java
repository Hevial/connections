package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import models.CompletedGame;
import models.GameState;
import models.PlayerCompletedGame;
import models.PlayerGameState;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends UDP notifications to registered clients.
 */
/**
 * Utility for sending UDP notifications to registered clients.
 *
 * <p>
 * This class is responsible for building and dispatching personalized
 * notification payloads to clients that have previously registered their
 * notification address via {@link NotificationRegistry}. Notifications may
 * include the final {@link CompletedGame} summary for the just-ended round,
 * a recipient-specific {@link PlayerGameState} (either reconstructed from the
 * new game snapshot or left empty for non-participants), a
 * {@link PlayerCompletedGame} view containing the recipient's per-game
 * statistics (or {@code null} if the recipient did not participate), and the
 * {@link GameState} of the newly started round so clients can refresh their
 * local game view.
 *
 * <p>
 * All network operations are best-effort UDP sends. Failures are logged
 * but do not throw to callers. The class is designed for concurrent use from
 * server threads; each invocation of {@link #notifyGameEnd(CompletedGame,
 * GameState)} creates its own ephemeral UDP socket for sending.
 */
public class NotificationSender {

    private static final Logger LOG = Logger.getLogger(NotificationSender.class.getName());
    private static final Gson GSON = new Gson();

    /**
     * Send a personalized game-end notification to all registered clients.
     *
     * <p>
     * For each registered recipient the method builds a JSON envelope that
     * always contains the provided {@code completedGame} summary. The envelope
     * also contains a recipient-specific {@link PlayerGameState} (created from
     * {@code newGameState}), a {@link PlayerCompletedGame} instance where the
     * {@code playerStats} field is {@code null} when the recipient did not
     * participate, and the {@code newGameState} snapshot. Each recipient
     * receives only their own {@code PlayerGameState}/{@code PlayerCompletedGame}
     * entry — other players' private state is never broadcast.
     *
     * <p>
     * This method performs best-effort UDP sends to the addresses returned
     * by {@link NotificationRegistry#getAllEntries()}. Errors while building
     * or sending individual packets are logged and do not interrupt the
     * overall notification loop.
     *
     * @param completedGame summary of the completed round; may be {@code null}
     *                      on first-run
     * @param newGameState  snapshot of the newly started round; used to create
     *                      starter {@link PlayerGameState} objects for
     *                      recipients that did not participate in the
     *                      completed round; may be {@code null}
     */
    public static void notifyGameEnd(CompletedGame completedGame, GameState newGameState) {
        try (DatagramSocket socket = new DatagramSocket()) {
            Map<String, InetSocketAddress> entries = NotificationRegistry.getAllEntries();
            for (Map.Entry<String, InetSocketAddress> entry : entries.entrySet()) {
                String userId = entry.getKey();
                InetSocketAddress addr = entry.getValue();

                String userPayload = createPayloadForUser(completedGame, newGameState, userId);
                byte[] data = userPayload.getBytes(StandardCharsets.UTF_8);

                LOG.info("NotificationSender: sending to " + addr + " userId=" + userId);
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

    /**
     * Build the JSON payload to send to a single recipient.
     *
     * <p>
     * The returned JSON string contains the following top-level fields when
     * available: {@code CompletedGame}, {@code PlayerGameState} (recipient's
     * live state or a fresh skeleton created from {@code newGameState}),
     * {@code PlayerCompletedGame} (recipient-specific summary or with
     * {@code playerStats==null} if the recipient did not participate), and
     * {@code NewGameState} (the newly started round snapshot).
     *
     * @param completedGame the completed-game summary; may be {@code null}
     * @param newGameState  the new game snapshot; may be {@code null}
     * @param userId        the recipient user id; used to select the correct
     *                      per-player stats in {@code completedGame}
     * @return JSON-encoded notification payload to send over UDP
     */
    private static String createPayloadForUser(CompletedGame completedGame, GameState newGameState, String userId) {
        JsonObject envelope = new JsonObject();
        envelope.add("CompletedGame", GSON.toJsonTree(completedGame));

        // Create a fresh PlayerGameState for the recipient based on the new game
        // snapshot
        if (newGameState != null) {
            LOG.info("gameId:" + newGameState.getGameId());
            LOG.info("words:" + newGameState.getAllWordsShuffled());
            LOG.info("time:" + newGameState.getRemainingTime());
            LOG.info("groups:" + newGameState.getGame().getGroups());

            try {
                PlayerGameState playerGameState = new PlayerGameState(newGameState.getGameId(),
                        newGameState.getAllWordsShuffled(),
                        newGameState.getRemainingTime(),
                        newGameState.getGame().getGroups());
                envelope.add("PlayerGameState", GSON.toJsonTree(playerGameState));
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to build PlayerGameState for user " + userId, e);
            }
        }

        // Build PlayerCompletedGame for this recipient: playerStats is null if user did
        // not participate
        try {
            models.PlayerGameStats stats = null;
            if (completedGame != null) {
                stats = completedGame.getPlayerStats(userId);
            }
            PlayerCompletedGame playerCompletedGame = new PlayerCompletedGame(
                    completedGame != null ? completedGame.getGameId()
                            : (newGameState != null ? newGameState.getGameId() : -1),
                    completedGame != null ? completedGame.getNumberOfPlayers() : 0,
                    completedGame != null ? completedGame.getNumberOfWinners() : 0,
                    completedGame != null ? completedGame.getNumberOfCompleters() : 0,
                    completedGame != null ? completedGame.getAverageScore() : 0.0,
                    completedGame != null ? completedGame.getGroups() : List.of(),
                    stats);
            envelope.add("PlayerCompletedGame", GSON.toJsonTree(playerCompletedGame));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to build PlayerCompletedGame for user " + userId, e);
        }

        if (newGameState != null) {
            envelope.add("NewGameState", GSON.toJsonTree(newGameState));
        }

        return envelope.toString();
    }
}
