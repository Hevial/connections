package server.handlers;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import models.CompletedGame;
import models.PlayerCompletedGame;
import models.PlayerGameState;
import models.Response;
import models.enums.Action;
import models.enums.StatusCodes;
import server.GameManager;
import server.Session;
import server.db.DBManager;

/**
 * Handler for {@code GAME_STATUS} requests.
 *
 * <p>Accepts a JSON payload containing a {@code gameId}. If the requested id
 * corresponds to the currently active round (or {@code -1} is supplied), the
 * handler returns the live {@link PlayerGameState} for the requesting session.
 * Otherwise, it attempts to load the completed game with the requested id and
 * returns a {@link PlayerCompletedGame} view containing per-player statistics
 * for the requester.</p>
 */
public class GamestatusRequestHandler implements RequestActionHandler {

    private final GameManager gameManager;

    GamestatusRequestHandler(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public Response handle(JsonElement data, Session session) {

        try {

            int requestedGameId = data.getAsJsonObject().get("gameId").getAsInt();

            if (requestedGameId == -1 || requestedGameId == gameManager.getCurrentGameState().getGameId()) {
                PlayerGameState playerGameState = gameManager.getOrCreatePlayerState(session.getUserId());
                JsonElement gameData = new Gson().toJsonTree(Map.of("playerGameState", playerGameState));
                return new Response(Action.GAME_STATUS, StatusCodes.SUCCESS, "Game status retrieved successfully",
                        gameData);
            } else {
                DBManager dbManager = DBManager.getInstance();
                CompletedGame completedGame = dbManager.getCompletedGameById(requestedGameId);

                if (completedGame == null) {
                    return new Response(Action.GAME_STATUS, StatusCodes.NOT_FOUND,
                            "Completed game with ID " + requestedGameId + " not found", null);
                }

                PlayerCompletedGame playerCompletedGame = new PlayerCompletedGame(
                        completedGame.getGameId(),
                        completedGame.getNumberOfPlayers(),
                        completedGame.getNumberOfWinners(),
                        completedGame.getNumberOfCompleters(),
                        completedGame.getAverageScore(),
                        completedGame.getGroups(),
                        completedGame.getPlayerStats(session.getUserId()));
                JsonElement gameData = new Gson().toJsonTree(Map.of("playerCompletedGame", playerCompletedGame));
                return new Response(Action.GAME_STATUS, StatusCodes.SUCCESS, "Game status retrieved successfully",
                        gameData);
            }

        } catch (Exception e) {

            System.err.println("Error handling game status request: " + e.getMessage());
            return new Response(Action.GAME_STATUS, StatusCodes.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve game status: " + e.getMessage(), null);
        }
    }
}