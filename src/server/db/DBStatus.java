package server.db;

/**
 * Status codes returned by {@link server.db.DBManager} operations.
 */
public enum DBStatus {

    SUCCESS,

    USER_NOT_FOUND,
    WRONG_PASSWORD,
    USERNAME_ALREADY_EXISTS,
    USER_ALREADY_LOGGED_IN,

    DATABASE_ERROR
}