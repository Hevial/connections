package client.handlers;

import com.google.gson.JsonElement;

import client.menus.BaseMenu;
import models.enums.StatusCodes;

/**
 * Handler for processing server responses according to action type.
 *
 * Implementations interpret the {@link StatusCodes}, the message and any JSON
 * data provided by the server, update client state or UI, and decide the
 * next navigation/menu to display.
 */
public interface ResponseActionHandler {
    /**
     * Processes a server response and determines the next menu.
     *
     * @param statusCode  the status code returned by the server (non-{@code null})
     * @param message     the accompanying message (may be {@code null})
     * @param data        additional data as {@link com.google.gson.JsonElement}
     *                    (may be {@code null})
     * @param currentMenu the current menu, useful to decide the next navigation
     *                    (may be {@code null})
     * @return the next {@link BaseMenu} to display, or the same {@code currentMenu}
     *         if no transition is required; may return {@code null} when
     *         appropriate
     */
    BaseMenu handle(StatusCodes statusCode, String message, JsonElement data, BaseMenu currentMenu);
}
