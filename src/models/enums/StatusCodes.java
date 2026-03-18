package models.enums;

/**
 * HTTP-like status codes used by the server responses.
 *
 * <p>The numeric code mirrors common HTTP semantics and can be used by
 * clients to make decisions without parsing textual messages.
 */
public enum StatusCodes {
    SUCCESS(200),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    StatusCodes(int code) {
        this.code = code;
    }

    /**
     * Returns the numeric code associated with this status.
     *
     * @return numeric status code
     */
    public int getCode() {
        return code;
    }

    /**
     * Lookup a {@link StatusCodes} enum constant by its numeric code.
     *
     * @param code numeric status code
     * @return the matching {@link StatusCodes}
     * @throws IllegalArgumentException if the code is unknown
     */
    public static StatusCodes fromCode(int code) {
        for (StatusCodes status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
