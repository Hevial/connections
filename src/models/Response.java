package models;

import com.google.gson.JsonElement;

import models.enums.Action;
import models.enums.StatusCodes;

/**
 * Data transfer object representing a response sent from server to client.
 *
 * <p>
 * A Response contains the original {@link Action}, a {@link StatusCodes}
 * value describing the result, an optional human-readable message, an optional
 * JSON payload and an optional session username attached by the server when
 * appropriate.
 */
public class Response {
    private Action action;
    private StatusCodes statusCode;
    private String message;
    private JsonElement data;
    private String sessionUsername;

    /**
     * Construct a response.
     *
     * @param action     original action this response corresponds to
     * @param statusCode status code describing the result
     * @param message    optional human-readable message; may be null
     * @param data       optional JSON payload; may be null
     */
    public Response(Action action, StatusCodes statusCode, String message, JsonElement data) {
        this.action = action;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        this.sessionUsername = null;
    }

    /**
     * @return the {@link Action} this response corresponds to
     */
    public Action getAction() {
        return action;
    }

    /**
     * @return the {@link StatusCodes} returned by the server
     */
    public StatusCodes getStatusCode() {
        return statusCode;
    }

    /**
     * @return optional human-readable message describing the result
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return optional JSON payload included in the response, or null
     */
    public JsonElement getData() {
        return data;
    }

    /**
     * Returns session username attached to the response by the server when present.
     *
     * @return session username, or null if not set
     */
    public String getSessionUsername() {
        return sessionUsername;
    }

    /**
     * Attach a session username to this response. Used by server code to indicate
     * which session the response belongs to.
     *
     * @param sessionUsername the session username to attach
     */
    public void setSessionUsername(String sessionUsername) {
        this.sessionUsername = sessionUsername;
    }
}
