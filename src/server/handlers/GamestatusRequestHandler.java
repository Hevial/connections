package server.handlers;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import models.PlayerGameState;
import models.Response;
import models.enums.Action;
import models.enums.StatusCodes;
import server.GameManager;
import server.Session;

public class GamestatusRequestHandler implements RequestActionHandler {

    private final GameManager gameManager;

    GamestatusRequestHandler(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public Response handle(JsonElement data, Session session) {

        try {
            PlayerGameState playerGameState = gameManager.getOrCreatePlayerState(session.getUserId());
            JsonElement gameData = new Gson().toJsonTree(Map.of("playerGameState", playerGameState));

            return new Response(Action.GAME_STATUS, StatusCodes.SUCCESS, "Game status retrieved successfully",
                    gameData);
        } catch (Exception e) {

            System.err.println("Error handling game status request: " + e.getMessage());
            return new Response(Action.GAME_STATUS, StatusCodes.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve game status: " + e.getMessage(), null);
        }
    }
}