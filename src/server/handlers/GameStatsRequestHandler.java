package server.handlers;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import models.CompletedGame;
import models.GameState;
import models.OngoingGameStats;
import models.Response;
import models.enums.Action;
import models.enums.StatusCodes;
import server.GameManager;
import server.Session;
import server.db.DBManager;

public class GameStatsRequestHandler implements RequestActionHandler {

    public final GameManager gameManager;

    public GameStatsRequestHandler(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public Response handle(JsonElement data, Session session) {

        Gson gson = new Gson();
        int requestedGameId = data.getAsJsonObject().get("gameId").getAsInt();
        GameState currentGameState = gameManager.getCurrentGameState();

        if (requestedGameId == -1 || requestedGameId == currentGameState.getGameId()) {

            OngoingGameStats stats = new OngoingGameStats(
                    currentGameState.getGameId(),
                    currentGameState.getRemainingTime(),
                    gameManager.getPlayersInProgress(),
                    gameManager.getPlayersCompleted(),
                    gameManager.getPlayersWon());

            JsonElement resData = gson.toJsonTree(Map.of("OngoingGameStats", stats));
            return new Response(Action.GAME_STATS, StatusCodes.SUCCESS,
                    "Game stats retrieved successfully", resData);
        }

        DBManager dbManager = DBManager.getInstance();
        CompletedGame completedGame = dbManager.getCompletedGameById(requestedGameId);
        if (completedGame == null) {
            return new Response(Action.GAME_STATS, StatusCodes.NOT_FOUND,
                    "Game with id " + requestedGameId + " not found", null);
        }

        JsonElement resData = gson.toJsonTree(Map.of("CompletedGame", completedGame));
        return new Response(Action.GAME_STATS, StatusCodes.SUCCESS, "Game stats retrieved successfully", resData);
    }
}
