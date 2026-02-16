package client.handlers;

import com.google.gson.JsonElement;

import client.menus.BaseMenu;
import models.enums.StatusCodes;

/**
 * Defines a handler for processing server responses based on action type.
 * <p>
 * Implementations of this interface handle responses received from the server,
 * interpreting the status code, message, and any associated data to update the
 * client UI or state.
 * </p>
 *
 * @param statusCode the status code returned by the server
 * @param message    the message accompanying the response
 * @param data       any additional data included in the response
 * @return a {@link Menu} instance representing the next menu to display
 */
public interface ResponseActionHandler {
    BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu);
}
