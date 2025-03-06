package commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import utils.GCloudCLIExecutor;
import utils.GCloudStorageManager;
import utils.InvalidFormatException;

import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUrlCommand extends GCloudCommand<String> {
    private static final Pattern SIGNED_URL_PATTERN = Pattern.compile("(https://storage.googleapis.com/[^\\s]+)");
    private static final String serviceAccountKeyFilePath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

    private String duration;
    public String getKeyPath() {return serviceAccountKeyFilePath;}

    public SignUrlCommand(GCloudCLIExecutor executor, String bucketName, String duration) {
        super(executor, bucketName);
        this.duration = duration;
    }

    @Override
    protected void validateFormat(String output) throws InvalidFormatException {
        if (output == null || output.isEmpty()) {
            throw new InvalidFormatException("❌ sign-url output is empty or null");
        }

        Matcher matcher = SIGNED_URL_PATTERN.matcher(output);
        if (!matcher.find()) {
            throw new InvalidFormatException("❌ Failed to extract Signed URL from output: " + output);
        }
    }

    /**
     * Executes the `gcloud storage sign-url` command using the environment's service account.
     *
     * @param filePath Full GCS path of the file (e.g., gs://my-bucket/file.txt)
     * @return The signed URL string if successful
     * @throws Exception if the command fails or URL is invalid
     */
    /**
     * Extracts the service account email from the GOOGLE_APPLICATION_CREDENTIALS JSON file.
     */
    public static String getServiceAccountEmail() throws IOException {
        if (serviceAccountKeyFilePath == null || serviceAccountKeyFilePath.isEmpty()) {
            throw new RuntimeException("❌ GOOGLE_APPLICATION_CREDENTIALS is not set in the environment!");
        }

        try (FileReader reader = new FileReader(serviceAccountKeyFilePath)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            if (!jsonObject.has("client_email")) {
                throw new RuntimeException("❌ Service account email not found in credentials JSON.");
            }
            return jsonObject.get("client_email").getAsString();
        }
    }

    /**
     * Generates a signed URL for a given file path using gcloud CLI.
     */
    public String execute(String filePath) throws Exception {
        System.out.println("🛠️ Using Pre-Set GOOGLE_APPLICATION_CREDENTIALS for Signing URL...");

        // ✅ Retrieve the service account email
        String serviceAccountEmail = getServiceAccountEmail();
        System.out.println("📧 Using Service Account Email: " + serviceAccountEmail);

        // ✅ Create the command with --impersonate-service-account
        String command = String.format(
                "gcloud storage sign-url --duration=%s --impersonate-service-account=%s %s",
                duration, serviceAccountEmail, filePath
        );

        System.out.println("🛠️ Executing: " + command);

        // ✅ Run the command
        String output = GCloudCLIExecutor.executeCommand(command);

        // ✅ Validate format before extracting
        validateFormat(output);

        // ✅ Extract and return the signed URL
        Matcher matcher = SIGNED_URL_PATTERN.matcher(output);
        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            throw new RuntimeException("❌ Unexpected error: Signed URL was not extracted correctly.");
        }
    }


}
