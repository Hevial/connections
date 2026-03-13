package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import models.AuthRequest;
import models.GameState;
import models.PlayerGameState;
import models.Response;
import models.enums.Action;
import models.enums.StatusCodes;
import server.GameManager;
import server.Session;
import server.db.DBManager;
import server.db.DBStatus;

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

    private final GameManager gameManager;

    public LoginRequestHandler(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public Response handle(JsonElement data, Session session) {

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
        AuthRequest user = gson.fromJson(body, AuthRequest.class);
        String username = user.getUsername().trim();
        String password = user.getPassword();

        if (username.isEmpty() || password.trim().isEmpty()) {
            return new Response(Action.LOGIN, StatusCodes.BAD_REQUEST,
                    "Login failed: Invalid username or password", null);
        }

        // Authenticate user
        DBManager dbManager = DBManager.getInstance();
        try {

            DBStatus loginStatus = dbManager.loginUser(username, password);

            switch (loginStatus) {
                case USER_NOT_FOUND:
                    return new Response(Action.LOGIN, StatusCodes.NOT_FOUND,
                            "Login failed: User not found", null);
                case WRONG_PASSWORD:
                    return new Response(Action.LOGIN, StatusCodes.UNAUTHORIZED,
                            "Login failed: Invalid username or password", null);
                case USER_ALREADY_LOGGED_IN:
                    return new Response(Action.LOGIN, StatusCodes.CONFLICT,
                            "Login failed: User already logged in", null);
                case SUCCESS:
                    session.setUsername(username);
                    session.setUserId(dbManager.getUserByUsername(username).getUserId());
                    JsonObject gameData = new JsonObject();
                    gameData.addProperty("message", "Welcome back, " + username + "!");
                    gameData.addProperty("username", username);

                    GameState gameState = gameManager.getCurrentGameState();
                    if (gameState == null || gameState.getGame() == null) {
                        return new Response(Action.LOGIN, StatusCodes.INTERNAL_SERVER_ERROR,
                                "Login failed: No active game available", null);
                    }

                    PlayerGameState playerGameState = gameManager.getOrCreatePlayerState(session.getUserId());

                    gameData.add("playerGameState", gson.toJsonTree(playerGameState));

                    return new Response(Action.LOGIN, StatusCodes.SUCCESS, "Login successful", gameData);
                default:
                    return new Response(Action.LOGIN, StatusCodes.INTERNAL_SERVER_ERROR,
                            "Login failed: Server error", null);
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            return new Response(Action.LOGIN, StatusCodes.INTERNAL_SERVER_ERROR,
                    "Login failed: Server error", null);
        }

    }
}