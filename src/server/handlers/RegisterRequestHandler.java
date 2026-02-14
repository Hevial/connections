package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import models.Action;
import models.Response;
import models.User;
import models.StatusCodes;
import server.db.DBManager;

/**
 * Handles user registration requests.
 * <p>
 * This handler processes incoming registration requests by validating the
 * request body,
 * checking for required fields (username and password), and attempting to
 * register a new user
 * in the database. It returns appropriate responses based on the outcome of the
 * registration process,
 * including success, missing fields, invalid input, user already exists, or
 * server errors.
 * </p>
 *
 * <p>
 * Usage:
 * <ul>
 * <li>Validates that the request contains a JSON object with "username" and
 * "password".</li>
 * <li>Ensures that neither the username nor password is empty.</li>
 * <li>Attempts to add the new user to the database via {@link DBManager}.</li>
 * <li>Returns a {@link Response} indicating the result of the registration
 * attempt.</li>
 * </ul>
 * </p>
 *
 * @see RequestActionHandler
 * @see DBManager
 * @see Response
 */
public class RegisterRequestHandler implements RequestActionHandler {

    @Override
    public Response handle(JsonElement data) {
        // Validate request body
        if (data == null || !data.isJsonObject()) {
            return new Response(Action.REGISTER, StatusCodes.BAD_REQUEST.getCode(),
                    "Registration failed: Invalid request body", null);
        }

        // Validate required fields
        JsonObject body = data.getAsJsonObject();
        if (!body.has("username") || !body.has("password")) {
            return new Response(Action.REGISTER, StatusCodes.BAD_REQUEST.getCode(),
                    "Registration failed: Missing username or password", null);
        }

        // create User object from JSON
        Gson gson = new Gson();
        User user = gson.fromJson(body, User.class);
        String username = user.getUsername().trim();
        String password = user.getPassword();

        // Validate username and password
        if (username.isEmpty() || password.trim().isEmpty()) {
            return new Response(Action.REGISTER, StatusCodes.BAD_REQUEST.getCode(),
                    "Registration failed: Username or password is empty", null);
        }

        // Attempt to register the user in the database
        DBManager dbManager = DBManager.getInstance();
        try {
            User newUser = new User(username, password);
            if (dbManager.addNewUser(newUser)) {
                return new Response(Action.REGISTER, StatusCodes.SUCCESS.getCode(), "Registration successful", null);
            }
            return new Response(Action.REGISTER, StatusCodes.CONFLICT.getCode(),
                    "Registration failed: User already exists", null);
        } catch (RuntimeException e) {
            return new Response(Action.REGISTER, StatusCodes.INTERNAL_SERVER_ERROR.getCode(),
                    "Registration failed: Server error", null);
        }
    }

}
