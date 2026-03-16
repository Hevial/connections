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

                // if (!completedGame.isPlayerPresent(session.getUserId())) {
                // return new Response(Action.GAME_STATUS, StatusCodes.FORBIDDEN,
                // "Non hai partecipato a questa partita", null);
                // }

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