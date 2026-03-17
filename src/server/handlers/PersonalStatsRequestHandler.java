package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import models.Response;
import models.UserStats;
import models.enums.Action;
import models.enums.StatusCodes;
import server.Session;
import server.db.DBManager;

public class PersonalStatsRequestHandler implements RequestActionHandler {

    @Override
    public Response handle(JsonElement data, Session session) {

        Gson gson = new Gson();
        DBManager dbManager = DBManager.getInstance();
        String userId = session.getUserId();

        UserStats stats = dbManager.getUserStats(userId);

        if (stats != null) {
            JsonElement resData = gson.toJsonTree(stats, UserStats.class);
            return new Response(Action.PERSONAL_STATS, StatusCodes.SUCCESS,
                    "Personal stats retrieved successfully", resData);
        }

        return new Response(Action.PERSONAL_STATS, StatusCodes.NOT_FOUND,
                "No stats found for user " + session.getUsername(), null);

    }

}
