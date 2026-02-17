package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import models.Response;
import models.User;
import models.enums.Action;
import models.enums.StatusCodes;
import server.db.DBManager;

/**
 * Handles login requests by validating the provided JSON data for username and
 * password,
 * authenticating the user against the database, and returning an appropriate
 * response.
 * <p>
 * The handler expects a JSON object containing "username" and "password"
 * fields.
 * It performs the following steps:
 * <ul>
 * <li>Validates the request body and required fields.</li>
 * <li>Authenticates the user using the {@link DBManager}.</li>
 * <li>Returns a success response with optional game data if authentication
 * succeeds.</li>
 * <li>Returns error responses for invalid input, authentication failure, or
 * server errors.</li>
 * </ul>
 * </p>
 */
public class LoginRequestHandler implements RequestActionHandler {

    // TODO
    @Override
    public Response handle(JsonElement data) {

        // Validate request body
        if (data == null || !data.isJsonObject()) {
            return new Response(Action.LOGIN, StatusCodes.BAD_REQUEST, "Login failed: Invalid request body",
                    null);
        }

        // Validate required fields
        JsonObject body = data.getAsJsonObject();
        if (!body.has("username") || !body.has("password")) {
            return new Response(Action.LOGIN, StatusCodes.BAD_REQUEST,
                    "Login failed: Missing username or password", null);
        }

        // create User object from JSON
        Gson gson = new Gson();
        User user = gson.fromJson(body, User.class);
        String username = user.getUsername().trim();
        String password = user.getPassword();

        if (username.isEmpty() || password.trim().isEmpty()) {
            return new Response(Action.LOGIN, StatusCodes.BAD_REQUEST,
                    "Login failed: Invalid username or password", null);
        }

        // Authenticate user
        DBManager dbManager = DBManager.getInstance();
        try {
            // TODO FIX USING DBStatus INSTEAD OF BOOLEAN
            if (!dbManager.loginUser(username, password)) {
                return new Response(Action.LOGIN, StatusCodes.UNAUTHORIZED,
                        "Login failed: Invalid username or password", null);
            }

            // TODO: get actual game data for the user
            JsonObject gameData = new JsonObject();
            gameData.addProperty("message", "Welcome back, " + username + "!");
            gameData.addProperty("username", username);
            return new Response(Action.LOGIN, StatusCodes.SUCCESS, "Login successful", gameData);
            // TODO: END

        } catch (Exception e) {
            return new Response(Action.LOGIN, StatusCodes.INTERNAL_SERVER_ERROR,
                    "Login failed: Server error", null);
        }

    }
}