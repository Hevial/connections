package client.handlers;

import com.google.gson.JsonElement;

import models.StatusCodes;

public interface ResponseActionHandler {
    void handle(StatusCodes statusCode, String message, JsonElement data);
}
