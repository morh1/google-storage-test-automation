package commands;

/**
 * Represents the possible outcomes of executing the `gsutil rm` command.
 * This enum defines the status of a file removal operation, indicating whether
 * the file was successfully removed, did not exist, or produced an unexpected output.
 *
 * @author Mor Hanania
 * @version 1.0
 * @since 2025-03-06
 */
public enum RmCommandStatus {

    /** Indicates that the command returned no output. */
    NULL_OUTPUT(0),

    /** Indicates that the command successfully executed and produced a valid output. */
    CONTENT_OUTPUT(1),

    /** Indicates that the specified file does not exist in Google Cloud Storage. */
    DOES_NOT_EXIST(2);

    /** Numeric code representing the status. */
    private final int code;

    /**
     * Constructs an {@code RmCommandStatus} enum with the specified status code.
     *
     * @param code The numeric code representing the status.
     */
    RmCommandStatus(int code) {
        this.code = code;
    }

}
