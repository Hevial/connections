package server.handlers;

import com.google.gson.JsonElement;
import models.Response;
import server.Session;

public interface RequestActionHandler {
    Response handle(JsonElement data, Session session);
}
