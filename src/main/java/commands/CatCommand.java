package commands;
import utils.GCloudCLIExecutor;
import utils.InvalidFormatException;

/**
 * Executes the `gsutil cat` command to retrieve the contents of files stored in Google Cloud Storage.
 * This command fetches the content of one or more files from a specified GCS bucket
 * using the Google Cloud CLI.
 *
 * @author Mor Hanania
 * @version 1.0
 * @since 2025-03-06
 */
public class CatCommand extends GCloudCommand<String> {

    /**
     * Constructs a {@code CatCommand} instance with the given CLI executor and bucket name.
     *
     * @param executor   The {@link GCloudCLIExecutor} responsible for executing Google Cloud commands.
     * @param bucketName The name of the Google Cloud Storage bucket.
     */
    public CatCommand(GCloudCLIExecutor executor, String bucketName) {
        super(executor, bucketName);
    }

    /**
     * Validates the format of the `gsutil cat` output.
     * This method ensures that the command output is not empty or null.
     *
     * @param output The raw output from the `gsutil cat` command.
     * @throws InvalidFormatException If the output is empty or null.
     */
    @Override
    protected void validateFormat(String output) throws InvalidFormatException {
        if (output == null || output.isEmpty()) {
            throw new InvalidFormatException("cat output is empty or null");
        }
    }

    /**
     * Executes the `gsutil cat` command for a single file.
     * This method retrieves the content of a single file stored in Google Cloud Storage.
     *
     * @param filePath The file path in the Google Cloud Storage bucket.
     * @return The file content as a string.
     * @throws Exception If an error occurs during execution or validation.
     */
    @Override
    public String execute(String filePath) throws Exception {
        return execute(new String[]{filePath});
    }

    /**
     * Executes the `gsutil cat` command for multiple files.
     * This method retrieves the content of multiple files from Google Cloud Storage
     * and returns the concatenated content as a single string.
     *
     * @param filePaths The file paths in the Google Cloud Storage bucket.
     * @return The concatenated content of the specified files.
     * @throws Exception If an error occurs during execution, validation, or if no file paths are provided.
     */
    public String execute(String... filePaths) throws Exception {
        if (filePaths == null || filePaths.length == 0) {
            throw new IllegalArgumentException("At least one file path must be provided.");
        }

        String files = String.join(" ", filePaths); // Join multiple file paths with spaces
        String output = executor.executeCommand("gsutil cat " + files);
        validateFormat(output);
        return output;
    }
}
