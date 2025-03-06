package commands;
import utils.GCloudCLIExecutor;
import utils.InvalidFormatException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a command for retrieving file sizes from Google Cloud Storage using `gsutil du`.
 * This class extends {@link GCloudCommand} to execute the `du` (disk usage) command
 * via Google Cloud CLI and parses the output to extract file sizes.
 *
 * @author Mor Hanania
 * @version 1.0
 * @since 2025-03-06
 */
public class DuCommand extends GCloudCommand<Map<String, String>> {

    /** A map storing file paths and their corresponding sizes in bytes. */
    private Map<String, String> fileSizeMap;

    /** Regular expression pattern for parsing `gsutil du` output. */
    private static final Pattern DU_OUTPUT_PATTERN = Pattern.compile("^(\\d+)\\s+(.+)$");

    /**
     * Constructs a {@code DuCommand} instance with the given CLI executor and bucket name.
     *
     * @param executor   The {@link GCloudCLIExecutor} responsible for executing Google Cloud commands.
     * @param bucketName The name of the Google Cloud Storage bucket.
     */
    public DuCommand(GCloudCLIExecutor executor, String bucketName) {
        super(executor, bucketName);
        this.fileSizeMap = new HashMap<>();
    }

    /**
     * Validates the format of the `gsutil du` output and extracts file sizes.
     * This method ensures that each line of the output follows the expected format
     * of "size path" and stores valid entries in {@code fileSizeMap}.
     *
     * @param output The raw output from the `gsutil du` command.
     * @throws InvalidFormatException If the output is empty, malformed, or contains invalid values.
     */
    @Override
    protected void validateFormat(String output) throws InvalidFormatException {
        if (output == null || output.isEmpty()) {
            throw new InvalidFormatException("du output is empty or null");
        }

        fileSizeMap.clear();
        String[] lines = output.split("\n");

        for (String line : lines) {
            Matcher matcher = DU_OUTPUT_PATTERN.matcher(line.trim());
            if (!matcher.matches()) {
                fileSizeMap = null;
                throw new InvalidFormatException("Invalid du output format: " + line);
            }

            String sizeStr = matcher.group(1);
            String path = matcher.group(2);
            long size = Long.parseLong(sizeStr);

            if (size < 0) {
                fileSizeMap = null;
                throw new InvalidFormatException("Negative file size found: " + size);
            }

            fileSizeMap.put(path, sizeStr);
        }
    }

    /**
     * Executes the `gsutil du` command for the specified file or directory.
     * This method runs the command via the CLI executor, validates the output format,
     * and stores the parsed file sizes in {@code fileSizeMap}.
     *
     * @param filePath The file or directory path in the Google Cloud Storage bucket.
     * @return A map where keys are file paths and values are file sizes in bytes.
     * @throws Exception If an error occurs during execution or validation.
     */
    @Override
    public Map<String, String> execute(String filePath) throws Exception {
        String output = executor.executeCommand("gsutil du " + filePath);
        validateFormat(output);
        return fileSizeMap;
    }

}
