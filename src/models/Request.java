package models;

import com.google.gson.JsonElement;

import models.enums.Action;

/**
 * Immutable data transfer object representing a request sent from client to
 * server.
 *
 * <p>
 * A Request contains an {@link Action} that identifies the operation to perform
 * and an optional JSON payload represented as a
 * {@link com.google.gson.JsonElement}.
 * The payload format depends on the {@code action} and is interpreted by the
 * corresponding request handler on the server side.
 */
public class Request {
    private Action action;
    private JsonElement data;

    /**
     * Create a new Request.
     *
     * @param action the action to perform, must not be null
     * @param data   optional JSON payload for the request; may be null
     */
    public Request(Action action, JsonElement data) {
        this.action = action;
        this.data = data;
    }

    /**
     * Returns the action associated with this request.
     *
     * @return the {@link Action} for this request
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns the optional JSON payload of this request.
     *
     * @return a {@link com.google.gson.JsonElement} containing request data, or
     *         null
     */
    public JsonElement getData() {
        return data;
    }
}
