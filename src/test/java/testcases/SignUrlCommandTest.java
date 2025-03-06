package testcases;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import commands.SignUrlCommand;
import org.testng.Assert;
import org.testng.annotations.*;
import utils.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TestNG test class for validating signed URLs generated for Google Cloud Storage (GCS).
 * Uses Playwright to verify download functionality of the signed URL.
 */
public class SignUrlCommandTest {
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String TEST_FILE_PATH = Paths.get(TEMP_DIR, "testfile.txt").toString();
    private static final String BUCKET_NAME = "sign-url-" + System.currentTimeMillis();
    private static final String FILE_NAME = "testfile.txt";
    private static final String SIGN_DURATION = "10m"; // URL validity duration

    private GCloudCLIExecutor executor;
    private SignUrlCommand signUrlCommand;
    private String signedUrl;

    /**
     * Sets up necessary GCloud IAM permissions, ensures the bucket exists, and uploads a test file.
     *
     * @throws Exception if any setup step fails.
     */
    @BeforeClass
    public void setup() throws Exception {
        executor = new GCloudCLIExecutor();
        signUrlCommand = new SignUrlCommand(executor, BUCKET_NAME, SIGN_DURATION);
        GCloudStorageManager.createBucket(BUCKET_NAME);
        String projectId = GCloudStorageManager.readProjectId();
        String serviceAccountEmail = signUrlCommand.getServiceAccountEmail();
        String userEmail = "morhanania@gmail.com";

        executor.executeCommand("gcloud iam service-accounts add-iam-policy-binding " + serviceAccountEmail +
                " --member=user:" + userEmail + " --role=roles/iam.serviceAccountTokenCreator --project=" + projectId);

        executor.executeCommand("gcloud iam service-accounts add-iam-policy-binding " + serviceAccountEmail +
                " --member=user:" + userEmail + " --role=roles/iam.serviceAccountUser --project=" + projectId);

        // Create and upload a test file
        FileCreator.createFile(TEST_FILE_PATH, 1024);
        GCloudStorageManager.uploadFile(BUCKET_NAME, TEST_FILE_PATH, FILE_NAME);
    }

    /**
     * Generates a signed URL for a file in GCS and validates its format.
     *
     * @throws Exception if signing fails.
     */
    @Test
    public void testGenerateSignedUrl() throws Exception {
        String gcsFilePath = "gs://" + BUCKET_NAME + "/" + FILE_NAME;
        String serviceAccountEmail = signUrlCommand.getServiceAccountEmail();

        // Generate signed URL using impersonation
        String rawOutput = executor.executeCommand("gcloud storage sign-url " + gcsFilePath +
                " --duration=10m --impersonate-service-account=" + serviceAccountEmail);

        // Extract last non-empty line (actual signed URL)
        String[] lines = rawOutput.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            if (!lines[i].trim().isEmpty()) {
                signedUrl = lines[i].trim();
                break;
            }
        }

        // Remove "signed_url:" prefix if present
        if (signedUrl.startsWith("signed_url:")) {
            signedUrl = signedUrl.replace("signed_url:", "").trim();
        }

        // Validate signed URL format
        Assert.assertTrue(signedUrl.startsWith("https://storage.googleapis.com/"),
                "Signed URL format is incorrect! Received: " + signedUrl);
    }

    /**
     * Verifies that the signed URL correctly triggers a file download.
     * Uses Playwright to automate browser behavior.
     */
    @Test(dependsOnMethods = "testGenerateSignedUrl")
    public void testSignedUrlFileDownload() {
        Assert.assertNotNull(signedUrl, "Signed URL is null. Ensure testGenerateSignedUrl runs first!");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // Wait for download event instead of navigation
            Download download = page.waitForDownload(() ->
                    page.evaluate("window.open('" + signedUrl + "', '_blank');") // Open in a new tab
            );

            // Validate download
            Path downloadPath = download.path();
            Assert.assertNotNull(downloadPath, "No file was downloaded!");
            Assert.assertTrue(download.suggestedFilename().contains("testfile"), "Incorrect file downloaded!");
        }
    }

    /**
     * Cleans up test resources by deleting the test file from GCS and local storage.
     *
     * @throws Exception if cleanup fails.
     */
    @AfterClass
    public void cleanup() throws Exception {
        GCloudStorageManager.deleteFile(BUCKET_NAME, FILE_NAME);
        new File(TEST_FILE_PATH).delete();
    }
}
