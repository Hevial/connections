package server.handlers;

import com.google.gson.JsonElement;
import models.Response;
import server.Session;

/**
 * Functional interface for handling a specific {@link models.enums.Action}.
 *
 * <p>Implementations process the action-specific JSON payload and the
 * requester {@link server.Session}, producing a {@link Response} to be
 * returned to the client.</p>
 */
public interface RequestActionHandler {
    /**
     * Handle the incoming request payload for the configured action.
     *
     * @param data    action-specific JSON data (may be {@code null})
     * @param session session object representing the connected client
     * @return a non-null {@link Response} describing the outcome
     */
    Response handle(JsonElement data, Session session);
}
