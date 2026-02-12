package server.handlers;

import com.google.gson.JsonElement;
import models.Response;

public interface RequestActionHandler {
    Response handle(JsonElement data);
}
