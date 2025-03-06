package commands;
import utils.InvalidFormatException;
import utils.GCloudCLIExecutor;

/**
 * Abstract base class for executing Google Cloud Storage (GCS) commands.
 * This class provides a structure for executing commands using the Google Cloud CLI.
 * It ensures that each command is associated with a specific bucket and provides
 * validation mechanisms for command output formats.
 *
 *
 * @param <T> The type of result returned by the command execution.
 *
 * @author Mor Hanania
 * @version 1.0
 * @since 2025-03-06
 */
public abstract class GCloudCommand<T> {

    /** Executor for running Google Cloud CLI commands. */
    protected GCloudCLIExecutor executor;

    /** Name of the Google Cloud Storage bucket used in the command. */
    protected String bucketName;

    /**
     * Constructs a {@code GCloudCommand} with the specified CLI executor and bucket name.
     * @param executor The {@link GCloudCLIExecutor} responsible for running CLI commands.
     * @param bucketName The name of the Google Cloud Storage bucket.
     */
    public GCloudCommand(GCloudCLIExecutor executor, String bucketName) {
        this.executor = executor;
        this.bucketName = bucketName;
    }

    /**
     * Validates the format of the command output.
     * This method should be implemented in subclasses to check whether the output
     * returned by the command follows the expected format.
     *
     * @param output The output string from the executed command.
     * @throws InvalidFormatException If the output format is invalid.
     */
    protected abstract void validateFormat(String output) throws InvalidFormatException;

    /**
     * Executes the command for a specified file in the GCS bucket.
     * Subclasses must implement this method to perform the actual execution
     * of the command using the CLI executor.
     *
     * @param filePath The path of the file in the GCS bucket.
     * @return The result of the command execution, as defined by the subclass.
     * @throws Exception If an error occurs during command execution.
     */
    public abstract T execute(String filePath) throws Exception;
}
