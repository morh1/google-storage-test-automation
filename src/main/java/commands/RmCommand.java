package commands;

import utils.GCloudCLIExecutor;
import utils.InvalidFormatException;


/**
 * Executes the `gsutil rm` command to delete files from Google Cloud Storage.
 * <p>
 * This command removes a specified file from a GCS bucket using the Google Cloud CLI
 * and returns the status of the operation.
 * </p>
 *
 * @author Mor Hanania
 * @version 1.0
 * @since 2025-03-06
 */
public class RmCommand extends GCloudCommand<RmCommandStatus> {

    /**
     * Constructs an {@code RmCommand} instance with the given CLI executor and bucket name.
     *
     * @param executor   The {@link GCloudCLIExecutor} responsible for executing Google Cloud commands.
     * @param bucketName The name of the Google Cloud Storage bucket.
     */
    public RmCommand(GCloudCLIExecutor executor, String bucketName) {
        super(executor, bucketName);
    }

    /**
     * Validates the format of the `gsutil rm` output.
     * Ensures that the command output is not empty or null.
     *
     * @param output The raw output from the `gsutil rm` command.
     * @throws InvalidFormatException If the output is empty or null.
     */
    @Override
    protected void validateFormat(String output) throws InvalidFormatException {
        if (output == null || output.isEmpty()) {
            throw new InvalidFormatException("rm output is empty or null");
        }
    }

    /**
     * Executes the `gsutil rm` command to remove a file from Google Cloud Storage.
     * This method attempts to delete the specified file and returns the appropriate
     * status based on the command output.
     *
     * @param filePath The file path in the Google Cloud Storage bucket.
     * @return The status of the removal operation, represented by {@link RmCommandStatus}.
     */
    @Override
    public RmCommandStatus execute(String filePath){
        try {
            String output = executor.executeCommand("gsutil rm " + filePath);

            //Handle missing file case
            if (output.contains("No URLs matched") || output.contains("does not exist")) {
                System.out.println("⚠️ File not found: " + filePath);
                return RmCommandStatus.DOES_NOT_EXIST;
            } else if (output.isEmpty()) {
                return RmCommandStatus.NULL_OUTPUT;
            } else {
                return RmCommandStatus.CONTENT_OUTPUT;
            }

        } catch (Exception e) {
            return RmCommandStatus.DOES_NOT_EXIST; // Treat API error as "file not found"
        }
    }
}
