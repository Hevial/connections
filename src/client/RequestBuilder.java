package client;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import client.menus.BaseMenu;
import models.AuthRequest;
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
        AuthRequest newUser = menu.requestCredentials("Nuovo Username: ", "Nuova Password: ");

        if (newUser.getUsername().isBlank() && !newUser.getPassword().isBlank()) {
            newUser.setUsername(oldUser.getUsername());
        }

        if (newUser.getPassword().isBlank() && !newUser.getUsername().isBlank()) {
            newUser.setPassword(oldUser.getPassword());
        }

        JsonElement data = gson.toJsonTree(Map.of("oldUser", oldUser, "newUser", newUser));
        return new Request(Action.UPDATE_CREDENTIALS, data);
    }

    public Request buildGameStatusRequest() {
        menu.setCurrAction(MenuAction.REQUEST_GAME_STATUS.getDisplayName());
        return new Request(Action.GAME_STATUS, null);
    }

    public Request buildGameStatsRequest() {
        return new Request(Action.GAME_STATS, null);
    }

    public Request buildLeaderboardRequest() {
        return new Request(Action.LEADERBOARD, null);
    }

    public Request buildPersonalStatsRequest() {
        return new Request(Action.PERSONAL_STATS, null);
    }

    public Request buildMakeProposalRequest() {
        menu.setCurrAction(MenuAction.MAKE_PROPOSAL.getDisplayName());
        menu.showGameData();
        List<String> proposal = menu.getWordsForProposal();
        JsonElement data = gson.toJsonTree(Map.of("proposalWords", proposal));
        return new Request(Action.SUBMIT_PROPOSAL, data);
    }

}