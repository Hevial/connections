package client;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import client.menus.BaseMenu;
import models.AuthRequest;
import models.LeaderboardReq;
import models.Request;
import models.enums.Action;
import models.enums.MenuAction;

/**
 * Builds client requests for server actions.
 *
 * <p>
 * This class centralizes request creation and relies on the menu layer
 * for collecting user credentials and updating the current UI action label.
 * </p>
 */
public class RequestBuilder {
    private final Gson gson;
    private final BaseMenu menu;

    /**
     * Creates a new RequestBuilder bound to a menu context.
     *
     * @param menu menu used to read credentials and update current action text
     */
    public RequestBuilder(BaseMenu menu) {
        this.gson = new Gson();
        this.menu = menu;
    }

    /**
     * Builds a register request by asking username and password from UI.
     *
     * @return request configured for Action.REGISTER
     */
    public Request buildRegisterRequest() {
        menu.setCurrAction(MenuAction.REGISTER.getDisplayName());
        AuthRequest user = menu.requestCredentials("Username: ", "Password: ");
        if (user == null)
            return null;
        JsonElement data = gson.toJsonTree(user, AuthRequest.class);
        return new Request(Action.REGISTER, data);
    }

    /**
     * Builds a login request by asking username and password from UI.
     *
     * @return request configured for Action.LOGIN
     */
    public Request buildLoginRequest() {
        menu.setCurrAction(MenuAction.LOGIN.getDisplayName());
        AuthRequest user = menu.requestCredentials("Username: ", "Password: ");
        if (user == null)
            return null;
        JsonElement data = gson.toJsonTree(user, AuthRequest.class);
        return new Request(Action.LOGIN, data);
    }

    /**
     * Builds a logout request.
     *
     * @return request configured for Action.LOGOUT
     */
    public Request buildLogoutRequest() {
        return new Request(Action.LOGOUT, null);
    }

    /**
     * Builds an update-credentials request.
     *
     * <p>
     * If only one new field is provided, the missing field is inherited
     * from old credentials before serializing payload.
     * </p>
     *
     * @return request configured for Action.UPDATE_CREDENTIALS
     */
    public Request buildUpdateCredentialsRequest() {
        menu.setCurrAction(MenuAction.UPDATE_CREDENTIALS.getDisplayName());
        AuthRequest oldUser = menu.requestCredentials("Vecchio Username: ", "Vecchia Password: ");
        if (oldUser == null)
            return null;
        AuthRequest newUser = menu.requestCredentials("Nuovo Username: ", "Nuova Password: ");
        if (newUser == null)
            return null;

        if (newUser.getUsername().isBlank() && !newUser.getPassword().isBlank()) {
            newUser.setUsername(oldUser.getUsername());
        }

        if (newUser.getPassword().isBlank() && !newUser.getUsername().isBlank()) {
            newUser.setPassword(oldUser.getPassword());
        }

        JsonElement data = gson.toJsonTree(Map.of("oldUser", oldUser, "newUser", newUser));
        return new Request(Action.UPDATE_CREDENTIALS, data);
    }

    /**
     * Builds a GAME_STATUS request.
     * <p>
     * If {@code currentGame} is {@code true} the request asks the server for the
     * status of the current game (the payload will carry {@code gameId: -1}).
     * Otherwise the method prompts the user (via the configured {@code menu}) to
     * enter a non-negative numeric game id; the prompt repeats until a valid id
     * is provided. The method sets the current menu action label before
     * collecting input.
     * </p>
     *
     * @param currentGame when {@code true} request the current game, when
     *                    {@code false} prompt for a specific game id
     * @return a {@link Request} with action {@link models.enums.Action#GAME_STATUS}
     *         and a JSON payload containing the selected {@code gameId}
     */
    public Request buildGameStatusRequest(boolean currentGame) {
        menu.setCurrAction(MenuAction.REQUEST_GAME_STATUS.getDisplayName());
        int gameId = -1;
        if (!currentGame) {
            gameId = menu.getGameId();
            if (gameId == Integer.MIN_VALUE)
                return null;
        }
        JsonElement data = gson.toJsonTree(Map.of("gameId", gameId));
        return new Request(Action.GAME_STATUS, data);
    }

    /**
     * Builds a request asking for the status of the current game on the client.
     * <p>
     * Delegates to {@link #buildGameStatusRequest(boolean)} with {@code true}.
     * </p>
     *
     * @return request configured for Action.GAME_STATUS for the current game
     */
    public Request buildCurrentGameStatusRequest() {
        return buildGameStatusRequest(true);
    }

    /**
     * Builds a request asking for the status of a specific game by id.
     * <p>
     * Delegates to {@link #buildGameStatusRequest(boolean)} with {@code false}.
     * The builder will prompt the user for the game id.
     * </p>
     *
     * @return request configured for Action.GAME_STATUS for a specific game id
     */
    public Request buildGameStatusByIdRequest() {
        return buildGameStatusRequest(false);
    }

    /**
     * Build a request to fetch statistics for a specific game id.
     *
     * @return a {@link Request} with action {@link Action#GAME_STATS} or null
     *         if user input was cancelled
     */
    public Request buildGameStatsRequest() {
        int gameId = menu.getGameId();
        if (gameId == Integer.MIN_VALUE)
            return null;
        JsonElement reqData = gson.toJsonTree(Map.of("gameId", gameId));
        return new Request(Action.GAME_STATS, reqData);
    }

    /**
     * Build a request to retrieve leaderboard information based on
     * {@link LeaderboardReq} returned by the menu.
     *
     * @return a {@link Request} with action {@link Action#LEADERBOARD}
     */

    public Request buildLeaderboardRequest() {
        LeaderboardReq lbReq = menu.getLeaderboardRequest();
        if (lbReq == null)
            return null;
        JsonElement reqData = gson.toJsonTree(lbReq);
        return new Request(Action.LEADERBOARD, reqData);
    }

    /**
     * Build a request to retrieve statistics for the currently authenticated
     * user.
     *
     * @return a {@link Request} with action {@link Action#PERSONAL_STATS}
     */
    public Request buildPersonalStatsRequest() {
        return new Request(Action.PERSONAL_STATS, null);
    }

    /**
     * Build a submit-proposal request using the words collected from the menu.
     *
     * @return a {@link Request} with action {@link Action#SUBMIT_PROPOSAL} or
     *         null if input was cancelled
     */
    public Request buildMakeProposalRequest() {
        menu.setCurrAction(MenuAction.MAKE_PROPOSAL.getDisplayName());
        menu.showGameData();
        List<String> proposal = menu.getWordsForProposal();
        if (proposal == null)
            return null;
        JsonElement data = gson.toJsonTree(Map.of("proposalWords", proposal));
        return new Request(Action.SUBMIT_PROPOSAL, data);
    }

}