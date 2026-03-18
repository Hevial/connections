package server.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import models.GameState;
import models.PlayerGameState;
import models.Response;
import models.enums.Action;
import models.enums.StatusCodes;
import server.GameManager;
import server.Session;

/**
 * Handles {@code SUBMIT_PROPOSAL} requests for the current game round.
 *
 * <p>
 * Request payload:
 * </p>
 *
 * <pre>
 * {
 *   "proposalWords": ["WORD1", "WORD2", "WORD3", "WORD4"]
 * }
 * </pre>
 *
 * <p>
 * Validation flow:
 * </p>
 * <ul>
 * <li>Request body must be a JSON object with {@code proposalWords} array.</li>
 * <li>Proposal must contain exactly 4 distinct words.</li>
 * <li>All proposal words must belong to the current game.</li>
 * <li>The same group cannot be submitted twice.</li>
 * </ul>
 *
 * <p>
 * Response payload always includes the updated {@code playerGameState} snapshot
 * in {@code data.playerGameState} when available, so the client can refresh the
 * board without issuing a separate status request.
 * </p>
 */
public class SubmitProposalRequestHandler implements RequestActionHandler {

    private final GameManager gameManager;
    private final Gson gson;

    public SubmitProposalRequestHandler(GameManager gameManager) {
        this.gameManager = gameManager;
        this.gson = new Gson();
    }

    @Override
    public Response handle(JsonElement data, Session session) {

        PlayerGameState playerGameState = gameManager.getOrCreatePlayerState(session.getUserId());
        GameState currGameState = gameManager.getCurrentGameState();

        if (playerGameState == null || currGameState == null) {
            return new Response(Action.SUBMIT_PROPOSAL, StatusCodes.INTERNAL_SERVER_ERROR,
                    "Proposal submission failed: No active game state available", null);
        }

        JsonElement gameData = gson.toJsonTree(Map.of("playerGameState", playerGameState));

        if (data == null || !data.isJsonObject()) {
            return new Response(Action.SUBMIT_PROPOSAL, StatusCodes.BAD_REQUEST,
                    "Proposal submission failed: Invalid request body", gameData);
        }

        JsonObject body = data.getAsJsonObject();
        if (!body.has("proposalWords") || !body.get("proposalWords").isJsonArray()) {
            return new Response(Action.SUBMIT_PROPOSAL, StatusCodes.BAD_REQUEST,
                    "Proposal submission failed: Missing or invalid proposalWords", gameData);
        }

        JsonArray proposalWordsJson = body.getAsJsonArray("proposalWords");

        List<String> proposalWordsList = new ArrayList<>();
        for (JsonElement element : proposalWordsJson) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                return new Response(Action.SUBMIT_PROPOSAL, StatusCodes.BAD_REQUEST,
                        "Proposal submission failed: proposalWords must be an array of strings", gameData);
            }
            proposalWordsList.add(element.getAsString().trim().toUpperCase());
        }

        Set<String> proposalWords = new HashSet<>(proposalWordsList);

        if (playerGameState.isWinner()) {
            return new Response(Action.SUBMIT_PROPOSAL, StatusCodes.BAD_REQUEST,
                    "Proposal submission failed: Game already won, wait for next game starting at "
                            + currGameState.getEndingAtTime(),
                    gameData);
        }

        if (playerGameState.isLoser()) {
            return new Response(Action.SUBMIT_PROPOSAL, StatusCodes.BAD_REQUEST,
                    "Proposal submission failed: Maximum number of mistakes reached, wait for next game starting at "
                            + currGameState.getEndingAtTime(),
                    gameData);
        }

        if (proposalWords.size() < 4) {
            return new Response(Action.SUBMIT_PROPOSAL, StatusCodes.BAD_REQUEST,
                    "Proposal submission failed: 4 different words are required", gameData);
        }

        if (!playerGameState.areWordsValid(proposalWords)) {
            return new Response(Action.SUBMIT_PROPOSAL, StatusCodes.BAD_REQUEST,
                    "Proposal submission failed: All proposed words must be part of the current game", gameData);
        }

        if (playerGameState.hasFoundGroup(proposalWords)) {
            return new Response(Action.SUBMIT_PROPOSAL, StatusCodes.BAD_REQUEST,
                    "Proposal submission failed: Group already found", gameData);
        }

        String theme = currGameState.getProposalTheme(proposalWords);

        if (theme == null) {
            playerGameState.decrementScore();
            playerGameState.incrementMistakes();
            gameData = gson.toJsonTree(Map.of("playerGameState", playerGameState));
            return new Response(Action.SUBMIT_PROPOSAL, StatusCodes.SUCCESS,
                    "Proposta sbagliata", gameData);
        }

        playerGameState.incrementScore();
        playerGameState.addFoundGroup(theme, proposalWords);

        // Check if the player has found all groups except one, and if so, automatically
        // find the last group for them
        if (playerGameState.getWordsLeft().size() == 4) {
            Set<String> remainingWords = new HashSet<>(playerGameState.getWordsLeft());
            String remainingTheme = currGameState.getProposalTheme(remainingWords);
            if (remainingTheme != null) {
                playerGameState.addFoundGroup(remainingTheme, remainingWords);
            } else {
                System.err.println("Error: Remaining words do not form a valid group: " + remainingWords);
            }
        }

        gameData = gson.toJsonTree(Map.of("playerGameState", playerGameState));
        return new Response(Action.SUBMIT_PROPOSAL, StatusCodes.SUCCESS,
                "Proposta corretta", gameData);
    }

}
